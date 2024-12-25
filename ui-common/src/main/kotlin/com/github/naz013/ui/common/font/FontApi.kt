package com.github.naz013.ui.common.font

import android.graphics.Typeface

interface FontApi {
  fun getTypeface(code: Int): Typeface?
}
