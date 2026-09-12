package com.github.naz013.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.github.naz013.wear.sync.WearReminderRepository
import com.github.naz013.wear.ui.WearApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycleScope.launch { WearReminderRepository.refreshFromDataClient(this@MainActivity) }

    setContent {
      WearApp(reminders = WearReminderRepository.reminders)
    }
  }
}
