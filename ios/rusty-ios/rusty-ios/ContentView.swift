//
//  ContentView.swift
//  rusty-ios
//
//  Created by ravikup on 17/10/2022.
//

import SwiftUI

struct ContentView: View {
    let s = getName()
    var body: some View {
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundColor(.accentColor)
            Text(s)
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

func getName() -> String {
    let result = hello("Ravi kumar")
    let sr = String(cString: result!)
    hello_release(UnsafeMutablePointer(mutating: result))
    return sr
}
