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

    use jni::objects::{JClass, JString};
    use jni::sys::jstring;
    use jni::JNIEnv;

    #[no_mangle]
    pub extern "system" fn Java_io_mosip_greetings_Conversation_init(_env: JNIEnv, _class: JClass) {
        android_logger::init_once(
            android_logger::Config::default()
                .with_min_level(log::Level::Debug)
                .with_tag("RUST-LAYER"),
        );
    }

    #[no_mangle]
    pub extern "system" fn Java_io_mosip_greetings_Conversation_greet(
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
}
