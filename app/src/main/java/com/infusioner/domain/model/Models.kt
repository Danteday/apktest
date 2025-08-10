package com.infusioner.domain.model

data class Device(
  val id: String,
  val name: String,
  val ssid: String?,
  val password: String?,
  val ip: String,
  val status: DeviceStatus?,
  val currentRecipe: Recipe?
)
enum class DeviceStatus { Offline, Ready, Active }
data class Recipe(val id: String, val name: String, val temperature: Double, val frequency: Double, val durationSec: Int)
object Ids { fun new() = java.util.UUID.randomUUID().toString() }
