package io.mosip.greetings;

import java.util.Calendar;

public class Conversation {
    private native String greet(String input);

    public String callNativeOp(){
            return greet(Calendar.getInstance().getTime().toString());
    }

    public static native void init();
}
