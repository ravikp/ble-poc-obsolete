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

// Sequence of actions
// Broadcasting/Advertising -> Connecting -> Indicate Central when data available to read
class Peripheral: ChatManager {
   private lateinit var gattServer: BluetoothGattServer
    private lateinit var onConnect: () -> Unit
    private lateinit var onMessageReceived: (String) -> Unit

    private var centralDevice: BluetoothDevice? = null
    var advertising: Boolean = false

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
        this.onConnect = onConnect

        advertiser.startAdvertising(settings, data, advertisingCallback)
        Log.i("BLE Peripheral", "Started advertising: $data")

    }

    private fun advertiseData(service: BluetoothGattService): AdvertiseData? {
        return AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(service.uuid))
            .build()
    }

    private fun advertiseSettings(): AdvertiseSettings? {
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()
    }

    private fun getService(): BluetoothGattService {
        val service = BluetoothGattService(
            serviceUUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        val writeChar = BluetoothGattCharacteristic(
            WRITE_MESSAGE_CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE)

        val readChar = BluetoothGattCharacteristic(
            READ_MESSAGE_CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_INDICATE,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        // 2902 - GATT Descriptor UUID for Client characteristic configuration
        readChar.addDescriptor(BluetoothGattDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
            BluetoothGattDescriptor.PERMISSION_READ))

        service.addCharacteristic(writeChar)
        service.addCharacteristic(readChar)

        val status =  gattServer.addService(service)
        Log.i("BLE Peripheral","Added service $status" )

        return service
    }

    private val gattServerCallback: BluetoothGattServerCallback = object : BluetoothGattServerCallback(){
        override fun onDescriptorWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            descriptor: BluetoothGattDescriptor?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onDescriptorWriteRequest(
                device,
                requestId,
                descriptor,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )

            Log.i("BLE Peripheral", "Got descriptor write request with value $value for ${descriptor?.uuid}")

            if(responseNeeded) {
                Log.i("BLE Peripheral", "Sending response to descriptor write")
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, ByteArray(0))

            }
        }
        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
            Log.i("BLE Peripheral", "Notification sent to device: $device and status: $status")
        }

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
                "BLE Peripheral",
                "onCharacteristicWriteRequest characteristic=" + characteristic.uuid + " value=" + Arrays.toString(
                    value
                )
            )

            if(value != null) {
                 onMessageReceived(String(value))
            }

            if(responseNeeded) {
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            Log.d("BLE Peripheral", "onCharacteristicReadRequest requestId=$requestId offset=$offset")
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
                Log.i("BLE Peripheral", "Device connected. $device")
                device?.let {
                    centralDevice = it
                    onConnect()
                }
            } else {
                Log.i("BLE Peripheral", "Device got disconnected. $device $newState")
            }
        }
    }

    private val advertisingCallback = object: AdvertiseCallback(){
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            advertising = true

            Log.i("BLE Peripheral", "Advertising onStartSuccess")
        }

        override fun onStartFailure(errorCode: Int) {
            advertising = false
            super.onStartFailure(errorCode)
            Log.e("BLE Peripheral", "Advertising onStartFailure: $errorCode")
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
            Log.i("BLE Peripheral", "Sent notification to device $centralDevice from ${output.uuid}")
            gattServer.notifyCharacteristicChanged(centralDevice!!, output, false)
        }
    }

    override fun name(): String = "Peripheral"
}