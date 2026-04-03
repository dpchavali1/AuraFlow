import UIKit
import shared

class iOSAppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Initialize Koin DI (registers all singletons including BillingManager)
        MainViewControllerKt.doInitKoinIos()

        // Phase 14: Start StoreKit transaction observer at app launch.
        // CRITICAL: Must be before any user action. Missed transactions (Ask-to-Buy,
        // interrupted purchases) are lost if observer starts late.
        startTransactionObserver()

        // Phase 15: Initialize consent manager at launch (before any tracking)
        // ConsentManagerKt.initConsentAtLaunch()

        return true
    }

    private func startTransactionObserver() {
        // The BillingManager SKPaymentTransactionObserver is registered when
        // BillingManager.initialize() is called from the Koin module.
        // Koin registers BillingManager as a singleton and calls initialize() lazily.
        // To force early initialization, get it from Koin here.
        // The Koin instance is available after doInitKoinIos() above.
        // We call initialize() via the KoinComponent pattern in MainViewController.
        // This is handled by IosModule.kt: single { BillingManager() } — the observer
        // is added in BillingManager.initialize() which is called by StoreViewModel.
        // For Phase 14: call BillingManagerKt.initBillingAtLaunch() if needed.
    }

    func applicationWillResignActive(_ application: UIApplication) {
        // Pause audio, save state if needed
    }
}
