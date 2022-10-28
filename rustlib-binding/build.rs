use flapigen::{JavaConfig, LanguageConfig};
use std::{env, path::Path};

fn main() {
    env_logger::init();
    let out_dir = env::var("OUT_DIR").unwrap();
    let in_src = Path::new("src").join("java_glue.rs.in");
    let out_src = Path::new(&out_dir).join("java_glue.rs");
    //ANCHOR: config
    let swig_gen = flapigen::Generator::new(LanguageConfig::JavaConfig(
        JavaConfig::new(
            Path::new("output")
                .join("java_files")
                .join("src")
                .join("main")
                .join("java")
                .join("io")
                .join("mosip"),
            "io.mosip".into(),
        )
        .use_null_annotation_from_package("android.support.annotation".into()),
    ))
    .rustfmt_bindings(true);
    //ANCHOR_END: config
    swig_gen.expand("android bindings", &in_src, &out_src);
    println!("cargo:rustc-link-search={}", create_tmp_libgcc());
    println!("cargo:rerun-if-changed={}", in_src.display());
}

// Rust (1.56 as of writing) still requires libgcc during linking, but this does
// not ship with the NDK anymore since NDK r23 beta 3.
// See https://github.com/rust-lang/rust/pull/85806 for a discussion on why libgcc
// is still required even after replacing it with libunwind in the source.
// XXX: Add an upper-bound on the Rust version whenever this is not necessary anymore.

// Implementation as per here - https://github.com/rust-windowing/android-ndk-rs/blob/21b11feff1b612656558d435908249faf78c980f/ndk-build/src/cargo.rs#L83
fn create_tmp_libgcc() -> String {
    let cargo_apk_link_dir = Path::new("target").join("tmp");
    std::fs::create_dir_all(&cargo_apk_link_dir).expect("error creating folder path for temporary libgcc.a");
    let libgcc = cargo_apk_link_dir.join("libgcc.a");
    std::fs::write(&libgcc, "INPUT(-lunwind)").expect("error creating contents for libgcc.a");
    String::from(cargo_apk_link_dir.to_str().unwrap())
}
