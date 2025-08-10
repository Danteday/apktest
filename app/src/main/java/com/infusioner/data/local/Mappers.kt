package com.infusioner.data.local
import com.infusioner.domain.model.*
fun DeviceWithRecipe.toDomain() = Device(d.id,d.name,d.ssid,d.password,d.ip,d.status?.let{runCatching{DeviceStatus.valueOf(it)}.getOrNull()}, r?.let{Recipe(it.id,it.name,it.temperature,it.frequency,it.durationSec)})
fun RecipeEntity.toDomain() = Recipe(id,name,temperature,frequency,durationSec)
fun Recipe.toEntity() = RecipeEntity(id,name,temperature,frequency,durationSec)
