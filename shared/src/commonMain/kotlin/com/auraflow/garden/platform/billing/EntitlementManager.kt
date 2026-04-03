package com.auraflow.garden.platform.billing

import com.auraflow.garden.data.repository.PlayerRepository
import com.russhwolf.settings.Settings

/**
 * Reads purchase state from BillingManager, determines feature unlocks,
 * and persists to Settings (for fast access) and Room (for audit trail).
 *
 * hasWardenPass is the primary gate for premium content.
 * Cosmetics are unlocked individually.
 */
class EntitlementManager(
    private val billingManager: BillingManager,
    private val playerRepository: PlayerRepository,
    private val settings: Settings,
) {
    private val KEY_WARDEN_PASS = "entitlement_warden_pass"

    /**
     * Returns true if the player has purchased Warden's Pass.
     * Checks local Settings first for speed, then verifies with BillingManager.
     */
    fun hasWardenPass(): Boolean {
        // First check local cache
        if (settings.getBoolean(KEY_WARDEN_PASS, false)) return true
        // Then verify with platform billing
        return billingManager.hasPurchase(ProductIds.WARDEN_PASS)
    }

    /**
     * Syncs entitlements from the platform billing system.
     * Call on app launch and after any purchase event.
     */
    suspend fun syncEntitlements() {
        val hasPass = billingManager.hasPurchase(ProductIds.WARDEN_PASS)
        settings.putBoolean(KEY_WARDEN_PASS, hasPass)

        if (hasPass) {
            val progress = playerRepository.getProgress()
            if (!progress.hasWardenPass) {
                playerRepository.updateProgress(progress.copy(hasWardenPass = true))
            }
        }

        // Sync cosmetics
        val cosmeticProductMap = mapOf(
            ProductIds.AURA_SKIN_AURORA to Pair("aurora", "aura_skin"),
            ProductIds.AURA_SKIN_NEBULA to Pair("nebula", "aura_skin"),
            ProductIds.AURA_SKIN_EMBER to Pair("ember", "aura_skin"),
            ProductIds.LUMA_SKIN_OWL to Pair("owl", "luma_skin"),
            ProductIds.LUMA_SKIN_BUTTERFLY to Pair("butterfly", "luma_skin"),
        )
        for ((productId, cosmeticPair) in cosmeticProductMap) {
            if (billingManager.hasPurchase(productId)) {
                val (cosmeticId, cosmeticType) = cosmeticPair
                if (!playerRepository.isCosmeticUnlocked(cosmeticId)) {
                    playerRepository.unlockCosmetic(cosmeticId, cosmeticType)
                }
            }
        }
    }

    /**
     * Gates premium features. Returns false if Warden's Pass is not owned.
     * Stages 1-10 are always free.
     */
    fun isStageAvailable(stageId: Int): Boolean {
        return stageId <= 10 || hasWardenPass()
    }

    /**
     * Returns true if the cosmetic is available (default, or purchased).
     */
    suspend fun isCosmeticAvailable(cosmeticId: String): Boolean {
        if (cosmeticId == "default" || cosmeticId == "firefly") return true
        return playerRepository.isCosmeticUnlocked(cosmeticId)
    }
}
