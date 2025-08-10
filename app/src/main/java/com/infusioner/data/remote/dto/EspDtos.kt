package com.infusioner.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class StatusEspDto(
    val isActive: Boolean,
    val isPaused: Boolean,
    val currentTemperature: Double,
    val targetTemperature: Int,
    val remainingTime: Int,
    val isConfigured: Boolean? = null
)

@Serializable data class StartEspReq(val temperature: Double, val timeSec: Int)
@Serializable data class ExtendReq(val seconds: Int)
@Serializable data class SimpleResp(val ok: Boolean? = true, val message: String? = null)
