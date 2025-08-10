package com.infusioner.data.repository

import com.infusioner.data.local.*
import com.infusioner.data.remote.ESP32Api
import com.infusioner.data.remote.WebSocketManager
import com.infusioner.data.remote.dto.StatusEspDto
import com.infusioner.data.remote.dto.StartEspReq
import com.infusioner.domain.model.*
import com.infusioner.domain.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(private val db:AppDatabase): DeviceRepository{
  override fun observe() = db.device().observe().map{ it.map(DeviceWithRecipe::toDomain) }
  override suspend fun get(id:String) = db.device().get(id)?.toDomain()
  override suspend fun insert(d:Device){ db.device().insert(DeviceEntity(d.id,d.name,d.ssid,d.password,d.ip,d.status?.name,d.currentRecipe?.id)); d.currentRecipe?.let{ db.recipe().insert(it.toEntity()) } }
  override suspend fun rename(id:String, name:String) { db.device().rename(id,name) }
  override suspend fun delete(id:String) { db.device().delete(id) }
  override suspend fun assignRecipe(id:String, recipe:Recipe?){ recipe?.let{ db.recipe().insert(it.toEntity()) }; db.device().setRecipe(id, recipe?.id) }
}

@Singleton
class RecipeRepositoryImpl @Inject constructor(private val db:AppDatabase): RecipeRepository{
  override fun observe() = db.recipe().observe().map{ it.map(RecipeEntity::toDomain) }
  override suspend fun insert(r:Recipe) { db.recipe().insert(r.toEntity()) }
  override suspend fun delete(id:String) { db.recipe().delete(id) }
}

@Singleton
class DeviceStatusRepositoryImpl @Inject constructor(
  private val db:AppDatabase,
  private val retrofitBuilder: Retrofit.Builder,
  private val ws: WebSocketManager
): DeviceStatusRepository {
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val json = Json { ignoreUnknownKeys = true }

  override fun observe(deviceId: String): Flow<LiveStatus> =
    db.device().observe()
      .map { list -> list.firstOrNull { e -> e.d.id == deviceId }?.d?.status }
      .map { n -> runCatching { DeviceStatus.valueOf(n ?: "Offline") }.getOrDefault(DeviceStatus.Offline) }
      .map { st -> LiveStatus(st, 0f) }
      .distinctUntilChanged()

  override suspend fun ensure(deviceId: String, ip: String) {
    ws.connect(deviceId, ip)
    // WS live updates
    scope.launch {
      ws.events.filter { it.first == deviceId }.collect { (_, payload) ->
        val dto = runCatching { json.decodeFromString<StatusEspDto>(payload) }.getOrNull()
        dto?.let { s ->
          val st = when {
            s.isActive && !s.isPaused -> DeviceStatus.Active
            !s.isActive -> DeviceStatus.Ready
            else -> DeviceStatus.Ready
          }
          db.device().setStatus(deviceId, st.name)
        }
      }
    }
    // Initial HTTP status (suspend safely, with try/catch)
    try {
      val api = retrofitBuilder.baseUrl("http://$ip/").build().create(ESP32Api::class.java)
      val s = api.status()
      val st = if (s.isActive && !s.isPaused) DeviceStatus.Active else DeviceStatus.Ready
      db.device().setStatus(deviceId, st.name)
    } catch (_: Throwable) { /* ignore, will rely on WS or later calls */ }
  }

  override suspend fun start(deviceId: String, ip: String, t: Double, f: Double, secs: Int) {
    val api = retrofitBuilder.baseUrl("http://$ip/").build().create(ESP32Api::class.java)
    api.start(StartEspReq(temperature=t, timeSec=secs))
    db.device().setStatus(deviceId, DeviceStatus.Active.name)
  }
  override suspend fun stop(deviceId: String, ip: String) {
    val api = retrofitBuilder.baseUrl("http://$ip/").build().create(ESP32Api::class.java)
    api.stop()
    db.device().setStatus(deviceId, DeviceStatus.Ready.name)
  }
}
