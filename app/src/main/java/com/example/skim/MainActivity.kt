package com.example.skim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skim.theme.SkimTheme
import com.example.skim.ui.main.SkimMainViewModel

class MainActivity : ComponentActivity() {
  private val appContainer by lazy { SkimAppContainer(applicationContext) }
  private val skimViewModelFactory by lazy { SkimViewModelFactory(appContainer.repository) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      val viewModel: SkimMainViewModel = viewModel(factory = skimViewModelFactory)
      SkimTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation(viewModel)
        }
      }
    }
  }
}
