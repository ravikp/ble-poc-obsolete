package io.mosip.greetings.ble

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.*


class Peripheral(context: Context, uuid: String) {
    private val serviceUUID: UUID = UUIDHelper.uuidFromString(uuid)
    private val mBluetoothDevices: HashSet<BluetoothDevice> = HashSet();
    var advertising: Boolean = false;

    //BLE
    private val service: BluetoothGattService= BluetoothGattService(
            serviceUUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
    private val mBluetoothManager:BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private  val mBluetoothAdapter = mBluetoothManager.adapter

    private val mGattServerCallback: BluetoothGattServerCallback = object : BluetoothGattServerCallback(){
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            Log.d(
                "BLE",
                "onCharacteristicWriteRequest characteristic=" + characteristic.uuid + " value=" + Arrays.toString(
                    value
                )
            )
        }
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            Log.d("BLE", "onCharacteristicReadRequest requestId=$requestId offset=$offset")
            mGattServer.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                null
            )
        }
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)

            if(newState == BluetoothProfile.STATE_CONNECTED){
            Log.i("RNBLEModule", "Device connected. $device")
            device?.let {
                mBluetoothDevices.add(it)
                Log.i("RNBLEModule", mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT).toString())
            }
            } else {
                Log.i("RNBLEModule", "Device got disconnected. $device $newState")
            }
        }
    }
    private val advertisingCallback = object: AdvertiseCallback(){
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            advertising = true

            Log.i("RNBLEModule", "Advertising onStartSuccess");
        }

        override fun onStartFailure(errorCode: Int) {
            advertising = false
            super.onStartFailure(errorCode)
            Log.e("RNBLEModule", "Advertising onStartFailure: $errorCode");
        }
    }
    private val mGattServer = mBluetoothManager.openGattServer(context, mGattServerCallback)

    fun start() {
        val char = BluetoothGattCharacteristic(
            UUIDHelper.uuidFromString("2031"),
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ)
        service.addCharacteristic(char)
        mGattServer.addService(service)


        val advertiser = mBluetoothAdapter.bluetoothLeAdvertiser
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build();
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(service.uuid))
            .build()

        Log.i("RNBLEModule", data.toString());

        advertiser.startAdvertising(settings, data, advertisingCallback);
    }
}