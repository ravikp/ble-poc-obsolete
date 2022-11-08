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
    let private_key: [UInt8] = [176, 248, 152, 2, 121, 212, 223, 159, 56, 59, 253, 110, 153, 11, 69, 197, 252, 186, 28, 79, 190, 247, 108, 39, 185, 20, 29, 255, 80, 185, 121, 131, 252, 109, 239, 156, 33, 22, 23, 4, 81, 188, 242, 194, 118, 63, 169, 22, 150, 209, 33, 73, 23, 89, 145, 242, 38, 179, 77, 219, 226, 50, 225, 167];
    return jwtsign(privateKey: private_key, claims: "test-claims")
//    return sprinkle(input: "Ravi")
}
