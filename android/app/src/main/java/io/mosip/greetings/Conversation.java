package io.mosip.greetings;

import java.util.Calendar;

public class Conversation {
    private native String greet(String input);

    private native byte[] encrypt(byte[] receiver_pub_key, byte[] sender_priv_key, String data);
//    private native byte[] decrypt(byte[] receiver_sec_key, byte[] sender_pub_key);

    public String callNativeOp() {
        return greet(Calendar.getInstance().getTime().toString());
    }

    public byte[] callNativeEncrypt(byte[] receiverPubKey, byte[] senderPrivateKey, String data) {
        return encrypt(receiverPubKey, senderPrivateKey, data);
    }

//    public byte[] callNativeDecrypt(byte[] senderPubKey, byte[] receiverPrivKey) {
//        return decrypt(senderPubKey, receiverPrivKey);
//    }

    public static native void init();
}
