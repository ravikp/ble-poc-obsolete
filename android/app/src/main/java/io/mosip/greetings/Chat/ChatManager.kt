package io.mosip.greetings.chat

interface ChatManager {
    val name: String
    fun addMessageReceiver(onMessageReceived: (String) -> Unit)
    fun sendMessage(message: String)
}