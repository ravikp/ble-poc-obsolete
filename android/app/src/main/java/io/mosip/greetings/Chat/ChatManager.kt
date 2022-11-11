package io.mosip.greetings.chat

interface ChatManager {
    fun addMessageReceiver(onMessageReceived: (String) -> Unit)
    fun sendMessage(message: String)
}