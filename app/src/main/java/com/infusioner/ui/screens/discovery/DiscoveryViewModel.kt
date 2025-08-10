package com.infusioner.ui.screens.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infusioner.domain.model.Device
import com.infusioner.domain.model.Ids
import com.infusioner.domain.repository.DeviceRepository
import com.infusioner.domain.repository.DeviceStatusRepository
import com.infusioner.domain.discovery.DiscoveryItem
import com.infusioner.domain.discovery.DiscoveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoveryState(val scanning:Boolean=false, val results: List<DiscoveryItem> = emptyList())

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
  private val discovery: DiscoveryRepository,
  private val devices: DeviceRepository,
  private val statusRepo: DeviceStatusRepository
): ViewModel() {

  private val _state = MutableStateFlow(DiscoveryState())
  val state: StateFlow<DiscoveryState> = _state.asStateFlow()

  fun start() = viewModelScope.launch {
    _state.value = DiscoveryState(scanning = true, results = emptyList())
    discovery.discover().collectLatest { list ->
      _state.value = _state.value.copy(results = list, scanning = true)
    }
  }

  fun restart() { stop(); start() }
  fun stop() = discovery.stop()

  fun add(item: DiscoveryItem) = viewModelScope.launch {
    val id = Ids.new()
    val dev = Device(id, item.name ?: "ESP32", null, null, item.ip, null, null)
    devices.insert(dev)
    statusRepo.ensure(id, item.ip)
  }
}
