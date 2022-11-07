package io.mosip.greetings;

import java.util.Calendar;

public class Conversation {
    private native String greet(String input);

    private native byte[] encrypt(byte[] receiver_pub_key, byte[] sender_priv_key, String data);
    private native String jwtsign(byte[] private_key, String claims_subject);
    private native boolean jwtverify(byte[] public_key, String token);
    private native void bluetooth();

    public String callNativeOp() {
        return greet(Calendar.getInstance().getTime().toString());
    }

    public byte[] callNativeEncrypt(byte[] receiverPubKey, byte[] senderPrivateKey, String data) {
        return encrypt(receiverPubKey, senderPrivateKey, data);
    }

    public String callNativeJWTSign(byte[] privKey, String subData) {
        return jwtsign(privKey, subData);
    }

    public boolean callNativeJWTVerify(byte[] pubKey, String token) {
        return jwtverify(pubKey, token);
    }

    public void callBluetooth() {
        bluetooth();
    }

    public static native void init();
}
