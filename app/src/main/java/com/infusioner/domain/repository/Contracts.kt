package com.infusioner.domain.repository
import com.infusioner.domain.model.*
import kotlinx.coroutines.flow.Flow
interface DeviceRepository {
  fun observe(): Flow<List<Device>>
  suspend fun get(id: String): Device?
  suspend fun insert(d: Device)
  suspend fun rename(id: String, name: String)
  suspend fun delete(id: String)
  suspend fun assignRecipe(id: String, recipe: Recipe?)
}
data class LiveStatus(val status: DeviceStatus, val progress: Float)
interface DeviceStatusRepository {
  fun observe(deviceId: String): Flow<LiveStatus>
  suspend fun ensure(deviceId: String, ip: String)
  suspend fun start(deviceId: String, ip: String, t: Double, f: Double, secs: Int)
  suspend fun stop(deviceId: String, ip: String)
}
interface RecipeRepository {
  fun observe(): Flow<List<Recipe>>
  suspend fun insert(r: Recipe); suspend fun delete(id: String)
}
interface PreferencesRepository {
  fun isDark(): Flow<Boolean>
  suspend fun setDark(v: Boolean)
}
