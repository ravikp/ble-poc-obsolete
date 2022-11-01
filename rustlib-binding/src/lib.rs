use std::ffi::{CStr, CString};
use std::os::raw::c_char;

//////////////////// For iOS ////////////////////////

#[no_mangle]
pub unsafe extern "C" fn hello(to: *const c_char) -> *mut c_char {
    let c_str = CStr::from_ptr(to);
    let recipient = match c_str.to_str() {
        Ok(s) => s,
        Err(_) => "you",
    };

    CString::new(purerust::process(recipient))
        .unwrap()
        .into_raw()
}

#[no_mangle]
pub unsafe extern "C" fn hello_release(item: *mut c_char) {
    if item.is_null() {
        return;
    }

    let _ = CString::from_raw(item);
}

#[cfg(target_os = "android")]
#[allow(non_snake_case)]
mod android {
    use core::slice::{self};
    use jni::objects::{JClass, JString, ReleaseMode, AutoArray};
    use jni::sys::jstring;
    use jni::JNIEnv;
    use jni::sys::jbyteArray;
    use jni::sys::jbyte;

    #[no_mangle]
    pub extern "C" fn Java_io_mosip_greetings_Conversation_init(_env: JNIEnv, _class: JClass) {
        android_logger::init_once(
            android_logger::Config::default()
                .with_min_level(log::Level::Debug)
                .with_tag("RUST-LAYER"),
        );

        unsafe {
            let r = libsodium_sys::sodium_init();
            dbg!(r);
        }
    }

    #[no_mangle]
    pub extern "C" fn Java_io_mosip_greetings_Conversation_greet(
        env: JNIEnv,
        // This is the class that owns our static method. It's not going to be used,
        // but still must be present to match the expected signature of a static
        // native method.
        _class: JClass,
        input: JString,
    ) -> jstring {
        log::info!("{}", format!("inside greet"));


        // debug!("this is a debug {}", "message");
        // First, we have to get the string out of Java. Check out the `strings`
        // module for more info on how this works.
        let input: String = env
            .get_string(input)
            .expect("Couldn't get java string!")
            .into();

        log::info!("{}", format!("inside greet with data {}", input));

        // Then we have to create a new Java string to return. Again, more info
        // in the `strings` module.
        let output = env
            .new_string(format!("From Rust: Hello, {}!", input))
            .expect("Couldn't create java string!");

        // Finally, extract the raw pointer to return.
        output.into_raw()
    }

    // encrypt & decrypt a dynamic string 
    #[no_mangle]
    pub extern "C" fn Java_io_mosip_greetings_Conversation_encrypt(
        env: JNIEnv,
        _class: JClass,
        receiver_public_key: jbyteArray,
        sender_private_key: jbyteArray,
        data: JString) -> jbyteArray {
        log::info!("{}", format!("encyrpt func start"));
        let java_data: String = env.get_string(data).expect("expected string data").into();
        log::info!("{}", format!("data from java: {:?}", java_data));

        let pub_key = env.convert_byte_array(receiver_public_key).unwrap();
        let priv_key = env.convert_byte_array(sender_private_key).unwrap();

        log::info!("{}", format!("pub key from java: {:?}", pub_key));
        log::info!("{}", format!("priv key from java: {:?}", priv_key));

        let mut cipher_text: Vec<u8> = Vec::new();
        cipher_text.resize_with((libsodium_sys::crypto_box_MACBYTES + java_data.len() as u32) as usize, Default::default);

        libsodium_encrypt(java_data, pub_key, priv_key, &mut cipher_text);

        log::info!("{}", format!("encyrpt func end: {:?}", cipher_text));
        env.byte_array_from_slice(&cipher_text).unwrap()
    }

    fn libsodium_encrypt(text: String, pub_key: Vec<u8>, priv_key: Vec<u8>, cipher_text: &mut Vec<u8>) {
        let text_ptr: *const u8 = text.as_ptr();

        // let mut cipher_text = [0; (libsodium_sys::crypto_box_MACBYTES + TEXT.len() as u32) as usize];
        let cipher_text_ptr: *mut u8 = cipher_text.as_mut_ptr();
        
        let nonce = [0;libsodium_sys::crypto_box_NONCEBYTES as usize];
        let nonce_ptr: *const u8 = nonce.as_ptr();

        let pub_key_ptr = pub_key.as_ptr();
        let priv_key_ptr = priv_key.as_ptr();
        
        unsafe {
            let r = libsodium_sys::crypto_box_easy(cipher_text_ptr, text_ptr, text.len() as u64, nonce_ptr, pub_key_ptr, priv_key_ptr);
            log::info!("{}", format!("crypto operation: {:?}", r));
        }
    }
    // JWT sign & verification of dynamic string

}
