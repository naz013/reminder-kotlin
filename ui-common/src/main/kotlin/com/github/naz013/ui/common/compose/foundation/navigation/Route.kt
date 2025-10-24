package com.github.naz013.ui.common.compose.foundation.navigation

import kotlinx.serialization.Serializable

data object Route {
    @Serializable
    data object Home

    @Serializable
    data object Settings
}
