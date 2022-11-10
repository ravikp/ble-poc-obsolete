package io.mosip.greetings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.mosip.greetings.Chat.ChatActivity
import io.mosip.greetings.ble.Common
import io.mosip.greetings.ble.Peripheral
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Common.init(this@MainActivity,this)
        Common.startBluetooth(this@MainActivity)

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
        val peripheral = Peripheral(this, "A392")
        peripheral.start()

        findViewById<LinearLayout>(R.id.actionsLayout).let {
            it?.setVisibility(View.GONE)
        }
        findViewById<LinearLayout>(R.id.loaderLayout).let {
            it?.setVisibility(View.VISIBLE)
        }

        findViewById<TextView>(R.id.loaderText).let {
            it?.setText(getString(R.string.broadcastingMessage))
        }

        findViewById<Button>(R.id.cancelLoading).let {
            it?.setOnClickListener {
                this.stopBroadCasting()
            }
        }


//        Timer().schedule(500) { moveToChatActivity() }
        println("Waiting for central to connect")
    }

    private fun moveToChatActivity() {
            val intent = Intent(this@MainActivity, ChatActivity::class.java)
            startActivity(intent)
    }

    private fun stopBroadCasting() {
        findViewById<LinearLayout>(R.id.loaderLayout).let {
            it?.setVisibility(View.GONE)
        }

        findViewById<LinearLayout>(R.id.actionsLayout).let {
            it?.setVisibility(View.VISIBLE)
        }

        println("Stopping broadcast")
    }

    private fun startScanningForPeripheral(){
        findViewById<LinearLayout>(R.id.actionsLayout).let {
            it?.setVisibility(View.GONE)
        }
        findViewById<LinearLayout>(R.id.loaderLayout).let {
            it?.setVisibility(View.VISIBLE)
        }

        findViewById<TextView>(R.id.loaderText).let {
            it?.setText(getString(R.string.ScanningMessage))
        }

        findViewById<Button>(R.id.cancelLoading).let {
            it?.setOnClickListener {
                this.stopScanningForPeripheral()
            }
        }

        Timer().schedule(500) { moveToChatActivity() }
        println("Starting Scan")
    }

    private fun stopScanningForPeripheral() {
        findViewById<LinearLayout>(R.id.loaderLayout).let {
            it?.setVisibility(View.GONE)
        }

        findViewById<LinearLayout>(R.id.actionsLayout).let {
            it?.setVisibility(View.VISIBLE)
        }

        println("Stopping Scan")
    }
}