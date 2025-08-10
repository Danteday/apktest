package com.infusioner
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.infusioner.ui.navigation.AppNavHost
import com.infusioner.ui.theme.InfusionerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { InfusionerTheme { AppNavHost() } }
  }
}
