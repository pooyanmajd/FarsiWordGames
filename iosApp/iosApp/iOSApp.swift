import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinHelperKt.initLogging()
        KoinHelperKt.initKoin()
        _ = KoinHelperKt.getWordVerificationViewModel()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}