package com.github.naz013.ui.note

import android.content.Context
import android.graphics.Typeface

/**
 * Seam over app's `AssetsUtil` (flavor-gated font assets bundled in `app`), which `ui-note` can't
 * depend on. Implemented in `app` and bound via Koin there - see `AppNoteFontProvider`.
 */
interface NoteFontProvider {
  fun getTypeface(context: Context, code: Int): Typeface?
  fun getFontNames(): List<String>
}
