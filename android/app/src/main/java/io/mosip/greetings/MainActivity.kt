package io.mosip.greetings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import uniffi.identity.TodoList
import uniffi.identity.sprinkle
import java.lang.System.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.greetingField).let {
            it?.text = sprinkle("Text2 from rust ")
        }
    }
}