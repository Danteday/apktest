package com.infusioner.data.discovery

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubnetScanner @Inject constructor(private val client: OkHttpClient) {
  private fun localIpv4(): Inet4Address? {
    return NetworkInterface.getNetworkInterfaces().toList()
      .flatMap { it.inetAddresses.toList() }
      .filterIsInstance<Inet4Address>()
      .firstOrNull { it.isSiteLocalAddress }
  }

  private fun cidr24(addr: Inet4Address): List<String> {
    val bytes = addr.address
    val base = "${bytes[0].toInt() and 0xff}.${bytes[1].toInt() and 0xff}.${bytes[2].toInt() and 0xff}."
    return (1..254).map { base + it }
  }

  suspend fun scan(): List<String> = withContext(Dispatchers.IO) {
    val ip = localIpv4() ?: return@withContext emptyList()
    val list = cidr24(ip)
    val http = client.newBuilder().connectTimeout(300, TimeUnit.MILLISECONDS).readTimeout(400, TimeUnit.MILLISECONDS).build()
    coroutineScope {
      list.chunked(32).flatMap { chunk ->
        chunk.map { host ->
          async {
            runCatching {
              val req = Request.Builder().url("http://$host/status").build()
              http.newCall(req).execute().use { resp -> if (resp.isSuccessful && (resp.body?.string()?.contains("currentTemperature") == true)) host else null }
            }.getOrNull()
          }
        }.awaitAll().filterNotNull()
      }
    }
  }
}
