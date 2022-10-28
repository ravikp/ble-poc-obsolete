ios_inc_dir=./ios/include
ios_libs_dir=./ios/libs

shared_lib_dir=./rustlib-binding

shared_lib_output_dir=$(shared_lib_dir)/output
shared_lib_ios_output=$(shared_lib_output_dir)/ios
shared_lib_android_output=$(shared_lib_output_dir)/android
shared_lib_android_jni_output=$(shared_lib_output_dir)/android/jniLibs
shared_lib_generated_headers=$(shared_lib_output_dir)/headers
shared_lib_generated_java_files=$(shared_lib_output_dir)/java_files
shared_lib_generated_java_source_dir=$(shared_lib_generated_java_files)/src/main/java/io/mosip

android_root=./android
android_app_src_main=$(android_root)/app/src/main
android_jni_libs_dir=$(android_app_src_main)/jniLibs

setup_dirs:
	mkdir -p $(ios_inc_dir) $(ios_libs_dir) $(shared_lib_ios_output) $(shared_lib_android_output) $(shared_lib_generated_java_source_dir)
	mkdir -p $(shared_lib_android_jni_output) $(shared_lib_generated_headers)
	mkdir -p $(android_jni_libs_dir)/x86_64
	mkdir -p $(android_jni_libs_dir)/armeabi-v7a
	mkdir -p $(android_jni_libs_dir)/arm64-v8a

setup_tools:
	cargo install uniffi_bindgen

clean_dirs:
	rm -rf $(ios_inc_dir) $(ios_libs_dir) $(shared_lib_output) $(android_jni_libs_dir)

clean:
	rm -rf $(ios_inc_dir)/**/* $(ios_libs_dir)/**/* $(shared_lib_ios_output)/**/* $(shared_lib_android_output)/**/* $(shared_lib_generated_java_source_dir)/**/*
	rm -rf $(shared_lib_android_jni_output)/**/* $(shared_lib_generated_headers)/**/*
	rm -rf $(android_jni_libs_dir)/x86_64/**/*
	rm -rf $(android_jni_libs_dir)/armeabi-v7a/**/*
	rm -rf $(android_jni_libs_dir)/arm64-v8a/**/*

	cargo clean --manifest-path=$(shared_lib_dir)/Cargo.toml

build: setup_dirs build_ios_shared_lib _copy_shared_to_ios build_android_shared_lib _copy_shared_to_android

build_android_shared_lib_x86_64:
	cargo build --target x86_64-linux-android  --manifest-path=$(shared_lib_dir)/Cargo.toml

build_android_shared_lib_aarch64:
	cargo build --target aarch64-linux-android  --manifest-path=$(shared_lib_dir)/Cargo.toml

build_android_shared_lib_armv7:
	cargo build --target armv7-linux-androideabi  --manifest-path=$(shared_lib_dir)/Cargo.toml

build_android_shared_lib_i686:
	cargo build --target i686-linux-android  --manifest-path=$(shared_lib_dir)/Cargo.toml

build_android_shared_lib: build_android_shared_lib_x86_64 build_android_shared_lib_armv7 build_android_shared_lib_aarch64

build_ios_shared_lib:
	cargo build --manifest-path=$(shared_lib_dir)/Cargo.toml
	cbindgen $(shared_lib_dir)/src/lib.rs -l c > $(shared_lib_generated_headers)/rustylib.h
	cargo lipo --manifest-path=$(shared_lib_dir)/Cargo.toml

copy_shared_to_ios: build_ios_shared_lib _copy_shared_to_ios
copy_shared_to_android: build_android_shared_lib _copy_shared_to_android

_copy_shared_to_ios:
	cp -f $(shared_lib_generated_headers)/* $(ios_inc_dir)
	cp -f $(shared_lib_dir)/target/universal/debug/librustylib_binding.a $(shared_lib_ios_output)
	cp -f $(shared_lib_ios_output)/* $(ios_libs_dir)

_copy_shared_to_android:
	cp -f $(shared_lib_dir)/target/x86_64-linux-android/debug/librustylib_binding.so $(android_jni_libs_dir)/x86_64
	cp -f $(shared_lib_dir)/target/aarch64-linux-android/debug/librustylib_binding.so $(android_jni_libs_dir)/arm64-v8a
	cp -f $(shared_lib_dir)/target/armv7-linux-androideabi/debug/librustylib_binding.so $(android_jni_libs_dir)/armeabi-v7a

setup_ios: setup_dirs copy_shared_to_ios