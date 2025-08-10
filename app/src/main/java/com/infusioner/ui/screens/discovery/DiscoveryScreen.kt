package com.infusioner.ui.screens.discovery
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(onBack: ()->Unit, vm: DiscoveryViewModel = hiltViewModel()) {
  val state by vm.state.collectAsState()
  LaunchedEffect(Unit){ vm.start() }
  Scaffold(
    topBar = { TopAppBar(title = { Text("Автопоиск устройств") }, navigationIcon = { IconButton(onClick=onBack){ Text("Назад") } }) }
  ){ inner ->
    Column(Modifier.fillMaxSize().padding(inner).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      if (state.scanning) LinearProgressIndicator(Modifier.fillMaxWidth())
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { vm.restart() }) { Text("Сканировать") }
        OutlinedButton(onClick = { vm.stop() }) { Text("Стоп") }
      }
      Divider()
      Text("Найдены: ${state.results.size}")
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.results, key={ it.ip }){ r ->
          ElevatedCard {
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
              Column {
                Text(r.name ?: "ESP32", style=MaterialTheme.typography.titleMedium)
                Text("IP: ${r.ip}")
                Text("Источник: ${r.source}")
              }
              Button(onClick = { vm.add(r) }){ Text("Добавить") }
            }
          }
        }
      }
    }
  }
}
