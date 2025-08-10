package com.infusioner.ui.navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.infusioner.ui.screens.devices.DevicesScreen
import com.infusioner.ui.screens.discovery.DiscoveryScreen

@Composable
fun AppNavHost() {
  val nav = rememberNavController()
  Scaffold { padding ->
    NavHost(nav, startDestination = "devices") {
      composable("devices"){ 
        DevicesScreen(
          padding = padding,
          onDiscover = { nav.navigate("discover") }
        ) 
      }
      composable("discover"){
        DiscoveryScreen(onBack = { nav.navigateUp() })
      }
    }
  }
}
