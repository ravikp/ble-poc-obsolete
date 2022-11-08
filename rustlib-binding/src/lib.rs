pub mod androidjni;

uniffi_macros::include_scaffolding!("identity");

fn sprinkle(input: String) -> String {
    format!("MSG from G0D: From sprinkle...{}", input)
}
