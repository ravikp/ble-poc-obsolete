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
    private lateinit var bluetoothLeScanner: BluetoothLeScanner;
    private lateinit var onDeviceFound: () -> Unit
    private lateinit var bluetoothGatt: BluetoothGatt

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
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

                if (gatt != null) {
                    gatt.discoverServices()
                    bluetoothGatt = gatt
                }
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

    override val name: String
        get() = "Central"

    override fun addMessageReceiver(onMessageReceived: (String) -> Unit) {
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
        writeChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        bluetoothGatt.writeCharacteristic(writeChar)
    }

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
        peripheralDevice.connectGatt(
            context,
            true,
            bluetoothGattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
    }
}
