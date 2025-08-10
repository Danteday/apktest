package com.infusioner.domain.discovery

import kotlinx.coroutines.flow.Flow

data class DiscoveryItem(
    val ip: String,
    val name: String? = null,
    val source: String
)

interface DiscoveryRepository {
    /** Start discovery (NSD + subnet scan) and emit a deduplicated, growing list. */
    fun discover(): Flow<List<DiscoveryItem>>
    /** Stop any ongoing discovery and clear internal state. */
    fun stop()
}
