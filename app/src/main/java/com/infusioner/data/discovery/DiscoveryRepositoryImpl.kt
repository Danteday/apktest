package com.infusioner.data.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.infusioner.domain.discovery.DiscoveryItem
import com.infusioner.domain.discovery.DiscoveryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoveryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: OkHttpClient
) : DiscoveryRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null
    private val _devices = MutableStateFlow<List<DiscoveryItem>>(emptyList())

    override fun discover(): Flow<List<DiscoveryItem>> {
        if (job?.isActive == true) return _devices
        job = scope.launch {
            val nsd = nsdFlow()
            val scan = scanFlow()
            merge(nsd, scan)
                .scan(emptyList<DiscoveryItem>()) { acc, item -> if (acc.any { it.ip == item.ip }) acc else acc + item }
                .collect { _devices.value = it }
        }
        return _devices
    }

    override fun stop() { job?.cancel(); job = null; _devices.value = emptyList() }

    // === NSD ===
    private fun nsdFlow(types: List<String> = listOf("_infusioner._tcp.", "_infusionerws._tcp.")): Flow<DiscoveryItem> = callbackFlow {
        val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        val listeners = types.map { type ->
            object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(serviceType: String) {}
                override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                    nsd.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {}
                        override fun onServiceResolved(resolved: NsdServiceInfo) {
                            val host = resolved.host?.hostAddress ?: return
                            trySend(DiscoveryItem(ip = host, name = resolved.serviceName, source = "NSD"))
                        }
                    })
                }
                override fun onServiceLost(serviceInfo: NsdServiceInfo) {}
                override fun onDiscoveryStopped(serviceType: String) {}
                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {}
                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
            }
        }
        types.zip(listeners).forEach { (t, l) -> nsd.discoverServices(t, NsdManager.PROTOCOL_DNS_SD, l) }
        awaitClose { types.zip(listeners).forEach { (_, l) -> runCatching { nsd.stopServiceDiscovery(l) } } }
    }

    // === Subnet scan ===
    private fun scanFlow(): Flow<DiscoveryItem> = flow {
        val ip = localIpv4() ?: return@flow
        val bytes = ip.address
        val base = "${bytes[0].toInt() and 0xff}.${bytes[1].toInt() and 0xff}.${bytes[2].toInt() and 0xff}."
        val http = client.newBuilder()
            .connectTimeout(300, TimeUnit.MILLISECONDS)
            .readTimeout(400, TimeUnit.MILLISECONDS)
            .build()
        for (hostSuffix in 1..254) {
            val host = base + hostSuffix
            val ok = runCatching {
                val req = Request.Builder().url("http://$host/status").build()
                http.newCall(req).execute().use { resp ->
                    val b = resp.body?.string() ?: return@use false
                    resp.isSuccessful && ("currentTemperature" in b || "isActive" in b)
                }
            }.getOrNull() == true
            if (ok) emit(DiscoveryItem(ip = host, name = null, source = "SCAN"))
        }
    }.flowOn(Dispatchers.IO)

    private fun localIpv4(): Inet4Address? {
        return NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { it.isSiteLocalAddress }
    }
}
