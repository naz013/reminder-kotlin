package com.elementary.tasks.navigation.nav3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Phase-1 Nav3 migration shim: represents the entire pre-migration `home_nav.xml` Navigation
 * Component graph as a single Nav3 entry (see [LegacyHomeHostEntry]). Every screen still owned by
 * that graph (Settings tree, Calendar, Home, reminder preview/archive) is reachable through this
 * one key until it's folded into its own [NavKey] in a later migration phase.
 */
@Serializable
data object LegacyHomeNavKey : NavKey
