package io.mosip.greetings.ble

import java.util.*
import java.util.regex.Pattern

object UUIDHelper {
    // base UUID used to build 128 bit Bluetooth UUIDs
    const val UUID_BASE = "0000XXXX-0000-1000-8000-00805f9b34fb"

    // handle 16 and 128 bit UUIDs
    fun uuidFromString(uuid: String): UUID {
        var uuid = uuid
        if (uuid.length == 4) {
            uuid = UUID_BASE.replace("XXXX", uuid)
        }
        return UUID.fromString(uuid)
    }

    // return 16 bit UUIDs where possible
    fun uuidToString(uuid: UUID): String {
        val longUUID = uuid.toString()
        val pattern =
            Pattern.compile("0000(.{4})-0000-1000-8000-00805f9b34fb", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(longUUID)
        return if (matcher.matches()) {
            matcher.group(1)
        } else {
            longUUID
        }
    }
}