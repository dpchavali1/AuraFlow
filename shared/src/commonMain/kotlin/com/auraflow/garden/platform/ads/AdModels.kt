package com.auraflow.garden.platform.ads

enum class AdState {
    NOT_LOADED,
    LOADING,
    READY,
    SHOWING,
    FAILED,
}

enum class ConsentStatus {
    UNKNOWN,
    NOT_REQUIRED,
    REQUIRED,
    OBTAINED,
    DENIED,
}

sealed class AdRewardResult {
    data object Rewarded : AdRewardResult()
    data object Skipped : AdRewardResult()
    data class Failed(val message: String) : AdRewardResult()
}
