import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinHelperKt.doInitKoin()
        _ = KoinHelperKt.getWordVerificationViewModel()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}