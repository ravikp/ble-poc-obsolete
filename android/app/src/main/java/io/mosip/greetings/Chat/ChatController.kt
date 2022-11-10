package io.mosip.greetings.chat;

import io.mosip.greetings.ble.Peripheral

class ChatController(mode: Int?) {
    companion object {
        const val PERIPHERAL_MODE = 0
        const val CENTRAL_MODE = 1
    }

    public var manager: ChatManager = if (mode == PERIPHERAL_MODE) {
        Peripheral.getInstance()
    } else {
        //change it to central
        Peripheral.getInstance()
    };
}
