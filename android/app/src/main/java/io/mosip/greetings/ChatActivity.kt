package io.mosip.greetings

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.concurrent.schedule

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        val mRecyclerView = findViewById<RecyclerView>(R.id.messagesRecylerView)
        val mAdapter = MessagesAdapter()
        val sendButton = findViewById<Button>(R.id.sendBtn)


        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.adapter = mAdapter

        sendButton.setOnClickListener {
            val message = Message("Some text from self", true)
            mAdapter.addMessage(Message("Some text from other", false))
            mAdapter.addMessage(message)
        }
    }
}