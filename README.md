# Kickstarter for the re-usable shared lib based on rust to be used in android and iOS

* Install Rust
`curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh`

* Run the command
`mkdir android ios flutter rust`
  - create sub-directories for each codebase

* Change directory to rust directory and run the following command
`cargo init --name rustylib --lib`

* Add the following android target distributions for rust compilations being in rust sub-directory
`rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android`

* Add the following iOS target distributions for rust compilation
`rustup target add aarch64-apple-ios armv7-apple-ios armv7s-apple-ios x86_64-apple-ios i386-apple-ios`
  - On my machine mac book pro - intel based machine, I got error for adding the targets
  `armv7-apple-ios, armv7s-apple-ios, and i386-apple-ios`

# Install tools for iOS,

* run following commands

  `xcode-select --install`

* This cargo subcommand will help you create a universal library for use with iOS.
  `cargo install cargo-lipo`

* This tool will let you automatically create the C/C++11 headers of the library.
  `cargo install cbindgen`

# Install tools for Android

* Install Android and NDK from android studio

* Set ANDROID_HOME, and NDK_HOME
`echo "ANDROID_HOME=$HOME/Library/Android/sdk" >> ~/.zshrc`
`echo "NDK_HOME=$HOME/Library/Android/sdk/ndk/25.1.8937393" >> ~/.zshrc`

* Install rustup targets
`rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android`

* Install cargo-ndk
`cargo install cargo-ndk`

* And setup ANDROID_NDK_HOME for cargo ndk to work properly
`ANDROID_NDK_HOME=/Users/ravikup/Library/Android/sdk/ndk/25.1.8937393`

* Setup toolchains for android
```
mkdir ~/.NDK

$(ANDROID_HOME)/ndk-bundle/build/tools/make_standalone_toolchain.py --api 26 --arch arm64 --install-dir ~/.NDK/arm64;
$(ANDROID_HOME)/ndk-bundle/build/tools/make_standalone_toolchain.py --api 26 --arch arm --install-dir ~/.NDK/arm;
$(ANDROID_HOME)/ndk-bundle/build/tools/make_standalone_toolchain.py --api 26 --arch x86 --install-dir ~/.NDK/x86;
```

* If you are using NDK > v22.0, they use lunwind and migrated from libgcc. Without this fix, the NDK toolchain still uses libgcc. Make the following change. Create a file called libgcc.a in the lib directory of the target ndk and redirect to libunwind. Do this for every target. The solution is given at(https://stackoverflow.com/questions/68873570/how-do-i-fix-ld-error-unable-to-find-library-lgcc-when-cross-compiling-rust)

`echo "INPUT(-lunwind)" > /Users/ravikup/.NDK/x86_64/lib64/clang/14.0.6/lib/linux/x86_64/libgcc.a`
`echo "INPUT(-lunwind)" > /Users/ravikup/.NDK/arm64/lib64/clang/14.0.6/lib/linux/aarch64/libgcc.a`
`echo "INPUT(-lunwind)" > /Users/ravikup/.NDK/arm64/lib64/clang/14.0.6/lib/linux/aarch64/libgcc.a`

* To build android libs for all environments
```
cargo ndk -t x86_64-linux-android -t aarch64-linux-android -o ./jniLibs build --release
```