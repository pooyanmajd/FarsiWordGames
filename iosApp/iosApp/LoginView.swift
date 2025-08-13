import SwiftUI
import Shared

struct LoginView: View {
    private let vm = KoinHelperKt.getKoin().get(objCClass: SharedAuthViewModel.self) as! AuthViewModel

    var body: some View {
        VStack(spacing: 12) {
            Text("Sign in to continue").font(.headline)
            Button("Continue with Google") {
                // TODO: Trigger Google flow then pass idToken to vm.signInWithGoogle()
            }
            .buttonStyle(.borderedProminent)
            Button("Continue with Apple") {
                // TODO: Trigger Apple flow then pass idToken/nonce to vm.signInWithApple()
            }
            .buttonStyle(.bordered)
            Button("Skip for now") {
                vm.signInAnonymously()
            }
        }
        .padding()
    }
}