package com.auraflow.garden.platform.billing

/**
 * Product IDs shared between Android and iOS.
 * Android uses these directly. iOS prepends the bundle ID (com.auraflow.garden.).
 */
object ProductIds {
    const val WARDEN_PASS = "warden_pass"
    const val AURA_SKIN_AURORA = "aura_skin_aurora"
    const val AURA_SKIN_NEBULA = "aura_skin_nebula"
    const val AURA_SKIN_EMBER = "aura_skin_ember"
    const val LUMA_SKIN_OWL = "luma_skin_owl"
    const val LUMA_SKIN_BUTTERFLY = "luma_skin_butterfly"
    const val SEASONAL_PACK_WINTER = "seasonal_pack_winter"
    const val GIFT_GARDEN = "gift_garden"

    val ALL = listOf(
        WARDEN_PASS,
        AURA_SKIN_AURORA,
        AURA_SKIN_NEBULA,
        AURA_SKIN_EMBER,
        LUMA_SKIN_OWL,
        LUMA_SKIN_BUTTERFLY,
        SEASONAL_PACK_WINTER,
        GIFT_GARDEN,
    )
}
