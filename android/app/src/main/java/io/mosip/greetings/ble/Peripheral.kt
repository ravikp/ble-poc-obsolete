package io.mosip.greetings.ble

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.*

class Peripheral {
    private val uuid = "AB29"
    private lateinit var gattServer: BluetoothGattServer
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var onConnect: () -> Unit
    private lateinit var onMessageReceived: (String) -> Unit
    private val serviceUUID: UUID = UUIDHelper.uuidFromString(uuid)
    private var centralDevice: BluetoothDevice? = null
    var advertising: Boolean = false;

    companion object {
        @Volatile
        private lateinit var instance: Peripheral

        fun getInstance(): Peripheral {
            synchronized(this) {
                if (!::instance.isInitialized) {
                    instance = Peripheral()
                }
                return instance
            }
        }
    }

    fun start(context: Context, onConnect: () -> Unit) {
        val bluetoothManager:BluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val mBluetoothAdapter = bluetoothManager.adapter
        val advertiser = mBluetoothAdapter.bluetoothLeAdvertiser

        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)

        val service = getService()
        val settings = advertiseSettings()
        val data = advertiseData(service)
        this.onConnect = onConnect;

        advertiser.startAdvertising(settings, data, advertisingCallback);
        Log.i("BLEPeripheral", "Started advertising: $data")

    }

    private fun advertiseData(service: BluetoothGattService): AdvertiseData? {
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(service.uuid))
            .build()
        return data
    }

    private fun advertiseSettings(): AdvertiseSettings? {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build();
        return settings
    }

    private fun getService(): BluetoothGattService {
        val service = BluetoothGattService(
            serviceUUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        val char = BluetoothGattCharacteristic(
            UUIDHelper.uuidFromString("2031"),
            BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(char)
        gattServer.addService(service)
        return service
    }

    private val gattServerCallback: BluetoothGattServerCallback = object : BluetoothGattServerCallback(){
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

            if(value != null) {
                 onMessageReceived(String(value))
            }

            if(responseNeeded) {
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            Log.d("BLE", "onCharacteristicReadRequest requestId=$requestId offset=$offset")
            gattServer.sendResponse(
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
                    centralDevice = it
                    onConnect()
                    Log.i("RNBLEModule", bluetoothManager.getConnectionState(device, BluetoothProfile.GATT).toString())
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

    fun addMessageReceiver(onMessageReceived: (String) -> Unit) {
        this.onMessageReceived = onMessageReceived
    }
}