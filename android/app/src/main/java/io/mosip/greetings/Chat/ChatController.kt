package io.mosip.greetings.chat;

import io.mosip.greetings.ble.Central
import io.mosip.greetings.ble.Peripheral

class ChatController(mode: Int?) {
    var manager: ChatManager;
    var peerName: String;

    companion object {
        const val PERIPHERAL_MODE = 0
        const val CENTRAL_MODE = 1
    }

    init {
        if (mode == PERIPHERAL_MODE) {
            manager =  Peripheral.getInstance()
            peerName = "Central"
        } else {
            manager = Central.getInstance()
            peerName = "Peripheral"
        };
    }

}
