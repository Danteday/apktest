package com.infusioner.ui.screens.devices
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infusioner.domain.model.Device
import com.infusioner.domain.model.Ids
import com.infusioner.domain.repository.DeviceRepository
import com.infusioner.domain.repository.DeviceStatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
  private val repo: DeviceRepository,
  private val statusRepo: DeviceStatusRepository
): ViewModel(){
  val devices = repo.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  fun discover() = viewModelScope.launch {
    // минимальный пример "ручного" добавления localhost-like (заглушки автообнаружения будут добавлены позже)
    val id = Ids.new()
    repo.insert(Device(id,"ESP32",null,null,"192.168.4.1",null,null))
    statusRepo.ensure(id,"192.168.4.1")
  }
}
