package com.github.naz013.ui.common.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.github.naz013.ui.common.theme.ThemeProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject

abstract class ComposeBottomSheetDialogFragment : BottomSheetDialogFragment() {

  private val themeProvider: ThemeProvider by inject()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return ComposeView(requireContext()).apply {
      setContent {
        AppTheme(darkTheme = themeProvider.isDark) {
          FragmentContent()
        }
      }
    }
  }

  @Composable
  abstract fun FragmentContent()
}
