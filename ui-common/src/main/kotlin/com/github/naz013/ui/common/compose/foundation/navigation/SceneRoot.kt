package com.github.naz013.ui.common.compose.foundation.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.naz013.ui.common.compose.foundation.DynamicScreen

@Composable
fun SceneRoot() {
    val navController: NavHostController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()

    DynamicScreen(
        mobilePortrait = {
            SceneRootGraph(
                navHostController = navController
            )
        }
    )
}

@Composable
fun SceneRootGraph(
    navHostController: NavHostController,
) {
    NavHost(
        navController = navHostController,
        startDestination = Route.Home,
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable<Route.Home> {
        }

        composable<Route.Settings> {
        }
    }
}
