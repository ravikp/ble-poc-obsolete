package io.mosip.greetings.ble;

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import io.mosip.greetings.chat.ChatManager
import java.nio.charset.Charset


class Central : ChatManager {
    private var scanning: Boolean = false
    private var connected: Boolean = false
    private lateinit var peripheralDevice: BluetoothDevice
    private lateinit var onDeviceConnected: () -> Unit
    private lateinit var onMessageReceived: (String) -> Unit
    private lateinit var bluetoothLeScanner: BluetoothLeScanner;
    private lateinit var onDeviceFound: () -> Unit
    private lateinit var bluetoothGatt: BluetoothGatt

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.i("BLE", "Status of write is $status for ${characteristic?.uuid}")
        }
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.i("BLE", "Characteristic changed to ${String(characteristic.value)}")
            onMessageReceived(String(characteristic.value))
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.i("BLE", "$status + ${descriptor?.uuid}")

            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.i("BLE", "Subscribed to read messages from peripheral")
            } else {
                Log.i("BLE", "Failed to Subscribe to read messages from peripheral")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("BLE", "Failed to discover services")
                return;
            }
            Log.i("BLE", "discovered services: ${gatt?.services?.map { it.uuid }}")
            if (gatt != null) {
                bluetoothGatt = gatt
            }

            onDeviceConnected()
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("BLE", "Connected to the peripheral")
                connected = true

                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("BLE", "Disconnected from the peripheral")

                connected = false
                gatt?.disconnect()
                gatt?.close()
            }
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.i("BLE", "Found the device: $result")
            stopScan()
            super.onScanResult(callbackType, result)
            peripheralDevice = result.device
            onDeviceFound()
        }
    }

    companion object {
        @Volatile
        private lateinit var instance: Central

        fun getInstance(): Central {
            synchronized(this) {
                if (!::instance.isInitialized) {
                    instance = Central()
                }
                return instance
            }
        }
    }

    override fun addMessageReceiver(onMessageReceived: (String) -> Unit) {
        if (!connected) {
            Log.e("BLE", "Peripheral is not connected")
            return
        }

        this.onMessageReceived = onMessageReceived
        subscribeToMessages()

    }

    private fun subscribeToMessages() {
        Log.i("BLE", "Subscribing to read message char")
        val service = bluetoothGatt.getService(Peripheral.serviceUUID)
        val readChar = service.getCharacteristic(Peripheral.READ_MESSAGE_CHAR_UUID)
        bluetoothGatt.setCharacteristicNotification(readChar, true);

        val descriptor: BluetoothGattDescriptor =
            readChar.getDescriptor(UUIDHelper.uuidFromString("00002902-0000-1000-8000-00805f9b34fb"));
        descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        val status = bluetoothGatt.writeDescriptor(descriptor)
        Log.i("BLE", "Raised subscription to peripheral: $status")
    }

    override fun sendMessage(message: String) {
        if (!connected) {
            Log.e("BLE", "Peripheral is not connected")
            return
        }

        val service = bluetoothGatt.getService(Peripheral.serviceUUID)
        val writeChar = service.getCharacteristic(Peripheral.WRITE_MESSAGE_CHAR_UUID)
        val value = message.toByteArray(Charset.defaultCharset());
        writeChar.value = value
        writeChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        val status = bluetoothGatt.writeCharacteristic(writeChar)
        Log.i("ble", "Sent message to peripheral: $status")

    }

    override fun name(): String = "Central"

    fun startScanning(context: Context, onDeviceFound: () -> Unit) {
        this.onDeviceFound = onDeviceFound
        val bluetoothManager: BluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        val handler = Handler()

        handler.postDelayed({
            if (scanning)
                stopScan()
        }, 100000)

        scanning = true
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(Peripheral.serviceUUID))
            .build()

        bluetoothLeScanner.startScan(
            mutableListOf(filter),
            ScanSettings.Builder().build(),
            leScanCallback
        )
    }

    fun stopScan() {
        bluetoothLeScanner.stopScan(leScanCallback)
    }

    fun connect(context: Context, onDeviceConnected: () -> Unit) {
        Log.i("BLE", "Connecting to Peripheral")
        this.onDeviceConnected = onDeviceConnected


        val gatt = peripheralDevice.connectGatt(
            context,
            false,
            bluetoothGattCallback,
            BluetoothDevice.TRANSPORT_LE
        )

        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
    }

}
