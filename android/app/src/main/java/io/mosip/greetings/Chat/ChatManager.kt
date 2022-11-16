package io.mosip.greetings.chat

interface ChatManager {
    fun addMessageReceiver(onMessageReceived: (String) -> Unit)
    //Returns error if message not sent
    fun sendMessage(message: String): String?
    fun name():String
}