package com.auraflow.garden.data.network

import io.ktor.client.engine.HttpClientEngine

/**
 * Platform-specific HTTP engine factory.
 * Android actual: OkHttp engine
 * iOS actual: Darwin engine
 */
expect fun createPlatformHttpEngine(): HttpClientEngine
