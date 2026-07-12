package com.example.skim

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skim.ui.main.SkimMainViewModel
import com.example.skim.ui.main.SkimMainRoute

@Composable
fun MainNavigation(viewModel: SkimMainViewModel) {
  SkimMainRoute(viewModel = viewModel, modifier = Modifier.safeDrawingPadding().padding(16.dp))
}
