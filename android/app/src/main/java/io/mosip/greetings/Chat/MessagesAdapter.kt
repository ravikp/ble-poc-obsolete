package io.mosip.greetings.Chat

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import io.mosip.greetings.R
import android.widget.TextView
import androidx.cardview.widget.CardView
import java.util.ArrayList

class MessagesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val messageHistory = ArrayList<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            MINE -> return SendHolder(LayoutInflater.from(parent.context), parent)
            NON_MINE -> return ReceivedHolder(LayoutInflater.from(parent.context), parent)
        }
        return SendHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MessageHolder) {
            val (text, _) = messageHistory[position]
            (holder as MessageHolder).setText(text)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val (_, self) = messageHistory[position]
        return if (self) {
            MINE
        } else {
            NON_MINE
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return messageHistory.size
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return true
    }

    fun addMessage(message: Message) {
        messageHistory.add(message)
        notifyItemInserted(itemCount - 1)
    }

    private class ReceivedHolder(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ) : RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.component_message_received,
            parent,
            false
        )
    ), MessageHolder {
        var text: TextView
        var card: CardView

        init {
            text = itemView.findViewById(R.id.recievedText)
            card = itemView.findViewById(R.id.receivedMessageCard)
        }

        override fun setText(text: String?) {
            this.text.text = text
        }
    }

    private class SendHolder(inflater: LayoutInflater, parent: ViewGroup?) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.component_message_send, parent, false)),
        MessageHolder {
        var text: TextView
        var card: CardView

        init {
            text = itemView.findViewById(R.id.sentText)
            card = itemView.findViewById(R.id.sentMessageCard)
        }

        override fun setText(text: String?) {
            this.text.text = text
        }
    }

    internal interface MessageHolder {
        fun setText(text: String?)
    }

    companion object {
        private const val MINE = 0
        private const val NON_MINE = 1
    }
}