package com.infusioner.ui.screens.devices
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.infusioner.domain.model.Device

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(padding: PaddingValues, onDiscover: ()->Unit, vm: DevicesViewModel = hiltViewModel()) {
  val devices by vm.devices.collectAsState()
  Scaffold(
    topBar={ TopAppBar(title={ Text("Устройства") }) }, 
    floatingActionButton={ FloatingActionButton(onClick= onDiscover ){ Text("🔎") } }
  ) { inner ->
    Column(Modifier.fillMaxSize().padding(inner).padding(padding).padding(16.dp)) {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(devices, key={ it.id }){ d ->
          ElevatedCard { Column(Modifier.padding(12.dp)) {
            Text(d.name, style=MaterialTheme.typography.titleMedium)
            Text("IP: ${d.ip}")
            Text("Статус: ${d.status ?: com.infusioner.domain.model.DeviceStatus.Offline}")
          } }
        }
      }
    }
  }
}
