package io.mosip.greetings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import java.lang.System.*

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            loadLibrary("rustylib_binding")
            Conversation.init()
        }
    }

    fun loadRustyLib() {
        loadLibrary("rustylib_binding")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        Log.i("native", "Before loading the library")
//        loadRustyLib()
//        Log.i("native", "After loading the library")

        val conversation = Conversation()

        findViewById<TextView>(R.id.greetingField).let {
            it?.text = "Text from rust " + conversation.callNativeOp()
        }
    }
}