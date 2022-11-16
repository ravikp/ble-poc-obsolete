package io.mosip.greetings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import io.mosip.greetings.ble.Central
import io.mosip.greetings.ble.Common
import io.mosip.greetings.ble.Peripheral
import io.mosip.greetings.chat.ChatActivity
import io.mosip.greetings.chat.ChatController

class MainActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Common.requestForRequiredPermissions(this@MainActivity, this, this::showActionsView)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.any { it != 0 }) {
            showPermErrorView()
            return
        }
        Log.i("Permissions", "${grantResults.joinToString()} for $requestCode")

        Common.requestForRequiredPermissions(this@MainActivity, this, this::showActionsView)
    }

    private fun startBroadCasting() {
        val peripheral = Peripheral.getInstance()
        peripheral.start(this,
            onConnect = { moveToChatActivity(ChatController.PERIPHERAL_MODE) },
            updateLoadingText =  {
            runOnUiThread {
                updateLoadingText(it)
            }
        })

        showLoadingLayout()
        updateLoadingText(getString(R.string.broadcastingMessage))
        setCancelLoadingButton  {
            this.stopBroadCasting(peripheral)
        }

        Log.i("BLE", "Waiting for central to connect")
    }

    private fun stopBroadCasting(peripheral: Peripheral) {
        peripheral.stop()
        showActionsLayout()

        Log.i("BLE", "Stopping broadcast")
    }

    private fun startScanningForPeripheral() {
        showLoadingLayout()
        updateLoadingText(getString(R.string.ScanningMessage))
        setCancelLoadingButton {
            this.stopScanningForPeripheral()
        }

        val central = Central.getInstance()
        central.startScanning(this,
            onDeviceFound =  { central.connect(this) { moveToChatActivity(ChatController.CENTRAL_MODE) } },
            updateLoadingText = { runOnUiThread { updateLoadingText(it) } }
        )

        Log.i("BLE","Starting Scan")
    }

    private fun stopScanningForPeripheral() {
        showActionsLayout()

        Central.getInstance().stopScan()
        Log.i("BLE","Stopping Scan")
    }

    private fun setCancelLoadingButton(onClick: (View) -> Unit) {
        findViewById<Button>(R.id.cancelLoadingBtn).let {
            it?.setOnClickListener(onClick)
        }
    }

    private fun showActionsLayout() {
        findViewById<LinearLayout>(R.id.loaderLayout).let {
            it?.setVisibility(View.GONE)
        }

        findViewById<LinearLayout>(R.id.actionsLayout).let {
            it?.setVisibility(View.VISIBLE)
        }
    }

    private fun showLoadingLayout() {
        findViewById<LinearLayout>(R.id.actionsLayout).let {
            it?.setVisibility(View.GONE)
        }
        findViewById<LinearLayout>(R.id.loaderLayout).let {
            it?.setVisibility(View.VISIBLE)
        }
    }

    private fun showActionsView() {
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.centralButton).let {
            it?.setOnClickListener {
                this.startScanningForPeripheral()
            }
        }

        findViewById<Button>(R.id.peripheralButton).let {
            it?.setOnClickListener {
                this.startBroadCasting()
            }
        }
    }

    private fun showPermErrorView() {
        setContentView(R.layout.activity_main_error)
        findViewById<TextView>(R.id.errorText).text = getString(R.string.permission_error_message)
        findViewById<Button>(R.id.requestPermBtn).setOnClickListener {
            Common.requestForRequiredPermissions(
                this@MainActivity,
                this,
                this::showActionsView
            )
        }
    }

    private fun updateLoadingText(message: String) {
        findViewById<TextView>(R.id.loadingText).let {
            it?.setText(message)
        }
    }

    private fun moveToChatActivity(mode: Int) {
        val intent = Intent(this@MainActivity, ChatActivity::class.java)
        intent.putExtra("mode", mode)
        startActivity(intent)
    }
}