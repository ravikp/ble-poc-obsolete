package io.mosip.greetings.ble

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import io.mosip.greetings.chat.ChatManager
import java.util.*

class Peripheral: ChatManager {
   private lateinit var gattServer: BluetoothGattServer
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var onConnect: () -> Unit
    private lateinit var onMessageReceived: (String) -> Unit

    private var centralDevice: BluetoothDevice? = null
    var advertising: Boolean = false;

    companion object {
        @Volatile
        private lateinit var instance: Peripheral
        val serviceUUID: UUID = UUIDHelper.uuidFromString("AB29")
        val WRITE_MESSAGE_CHAR_UUID = UUIDHelper.uuidFromString("2031")
        val READ_MESSAGE_CHAR_UUID = UUIDHelper.uuidFromString("2032")

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
        return AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(service.uuid))
            .build()
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

        val writeChar = BluetoothGattCharacteristic(
            WRITE_MESSAGE_CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val readChar = BluetoothGattCharacteristic(
            READ_MESSAGE_CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_INDICATE,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        service.addCharacteristic(writeChar)
        service.addCharacteristic(readChar)
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
                characteristic.value
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

    override fun addMessageReceiver(onMessageReceived: (String) -> Unit) {
        this.onMessageReceived = onMessageReceived
    }

    override fun sendMessage(message: String) {
        val output = gattServer
            .getService(serviceUUID)
            .getCharacteristic(READ_MESSAGE_CHAR_UUID)

        output.setValue(message)

        if(centralDevice != null) {
            Log.i("BLE", "Sent notification to device")
            gattServer.notifyCharacteristicChanged(centralDevice!!, output, false)
        }
    }
}