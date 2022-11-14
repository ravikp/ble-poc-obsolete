package io.mosip.greetings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import io.mosip.greetings.ble.Central
import io.mosip.greetings.chat.ChatActivity
import io.mosip.greetings.ble.Common
import io.mosip.greetings.ble.Peripheral
import io.mosip.greetings.chat.ChatController

class MainActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {
    private lateinit var peripheral: Peripheral

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Common.requestForRequiredPermissions(this@MainActivity, this)
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
        showActionsView()
        Common.startBluetooth(this@MainActivity)
    }

    private fun showPermErrorView() {
        setContentView(R.layout.activity_main_error)
        findViewById<TextView>(R.id.errorText).text = getString(R.string.permission_error_message)
        findViewById<Button>(R.id.requestPermBtn).setOnClickListener {
            Common.requestForRequiredPermissions(this@MainActivity, this)
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

    private fun startBroadCasting() {
        peripheral = Peripheral.getInstance();
        peripheral.start(this) {
            moveToChatActivity(ChatController.PERIPHERAL_MODE)
        }

        findViewById<LinearLayout>(R.id.actionsLayout).let {
            it?.setVisibility(View.GONE)
        }
        findViewById<LinearLayout>(R.id.mainErrorLayout).let {
            it?.setVisibility(View.VISIBLE)
        }

        findViewById<TextView>(R.id.errorText).let {
            it?.setText(getString(R.string.broadcastingMessage))
        }

        findViewById<Button>(R.id.requestPermBtn).let {
            it?.setOnClickListener {
                this.stopBroadCasting()
            }
        }

        println("Waiting for central to connect")
    }

    private fun moveToChatActivity(mode: Int) {
        val intent = Intent(this@MainActivity, ChatActivity::class.java)
        intent.putExtra("mode", mode)
        startActivity(intent)
    }

    private fun stopBroadCasting() {
        findViewById<LinearLayout>(R.id.mainErrorLayout).let {
            it?.setVisibility(View.GONE)
        }

        findViewById<LinearLayout>(R.id.actionsLayout).let {
            it?.setVisibility(View.VISIBLE)
        }

        println("Stopping broadcast")
    }

    private fun startScanningForPeripheral() {
        findViewById<LinearLayout>(R.id.actionsLayout).let {
            it?.setVisibility(View.GONE)
        }
        findViewById<LinearLayout>(R.id.mainErrorLayout).let {
            it?.setVisibility(View.VISIBLE)
        }

        findViewById<TextView>(R.id.errorText).let {
            it?.setText(getString(R.string.ScanningMessage))
        }

        findViewById<Button>(R.id.requestPermBtn).let {
            it?.setOnClickListener {
                this.stopScanningForPeripheral()
            }
        }

        val central = Central.getInstance()
        central.startScanning(this) {
            central.connect(this) {
                moveToChatActivity(ChatController.CENTRAL_MODE)
            }
        }

        println("Starting Scan")
    }

    private fun stopScanningForPeripheral() {
        findViewById<LinearLayout>(R.id.mainErrorLayout).let {
            it?.setVisibility(View.GONE)
        }

        findViewById<LinearLayout>(R.id.actionsLayout).let {
            it?.setVisibility(View.VISIBLE)
        }

        Central.getInstance().stopScan()
        println("Stopping Scan")
    }
}