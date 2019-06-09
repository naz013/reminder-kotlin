package com.elementary.tasks.intro

import androidx.annotation.RawRes

data class IntroItem(val title: String?, val description: String?, @RawRes val image: Int = 0)
