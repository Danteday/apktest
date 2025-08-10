package com.infusioner.data.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NsdHelper @Inject constructor(@ApplicationContext private val context: Context) {
  private val nsd by lazy { context.getSystemService(Context.NSD_SERVICE) as NsdManager }
  fun discover(types: List<String> = listOf("_infusioner._tcp.", "_infusionerws._tcp.")): Flow<Pair<String, NsdServiceInfo>> = callbackFlow {
    val listeners = types.map { type ->
      object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(p0: String) { }
        override fun onServiceFound(serviceInfo: NsdServiceInfo) { nsd.resolveService(serviceInfo, object: NsdManager.ResolveListener{
          override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {}
          override fun onServiceResolved(resolved: NsdServiceInfo) { trySend(type to resolved) }
        }) }
        override fun onServiceLost(p0: NsdServiceInfo) {}
        override fun onDiscoveryStopped(p0: String) {}
        override fun onStartDiscoveryFailed(p0: String, p1: Int) {}
        override fun onStopDiscoveryFailed(p0: String, p1: Int) {}
      }
    }
    types.zip(listeners).forEach { (t, l) -> nsd.discoverServices(t, NsdManager.PROTOCOL_DNS_SD, l) }
    awaitClose { types.zip(listeners).forEach { (t, l) -> runCatching { nsd.stopServiceDiscovery(l) } } }
  }
}
