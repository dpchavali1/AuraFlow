import SwiftUI

@main
struct AuraFlowApp: App {
    @UIApplicationDelegateAdaptor(iOSAppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
        }
    }
}
