package com.github.naz013.feature.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.github.naz013.ui.common.R

interface SendIntentResolver {
  fun resolve(intent: Intent, title: String)
}

@Composable
fun rememberSendIntentResolver(): SendIntentResolver {
  val context = LocalContext.current
  return object : SendIntentResolver {
    override fun resolve(intent: Intent, title: String) {
      try {
        context.startActivity(Intent.createChooser(intent, title))
      } catch (_: Exception) {
        Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
      }
    }
  }
}
