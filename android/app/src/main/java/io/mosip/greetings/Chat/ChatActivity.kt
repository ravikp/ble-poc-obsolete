package io.mosip.greetings.Chat

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.mosip.greetings.R
import io.mosip.greetings.ble.Peripheral

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val peripheral = Peripheral.getInstance();

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        val mRecyclerView = findViewById<RecyclerView>(R.id.messagesRecylerView)
        val mAdapter = MessagesAdapter()
        val sendButton = findViewById<Button>(R.id.sendBtn)


        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.adapter = mAdapter


        val description = findViewById<TextView>(R.id.chatDescription)
        description.text = "Talking to Central"
        peripheral.addMessageReceiver {
            mAdapter.addMessage(Message(it,  false))
        }

        sendButton.setOnClickListener {
            val message = Message("Some text from self", true)
            mAdapter.addMessage(Message("Some text from other", false))
            mAdapter.addMessage(message)
        }
    }
}