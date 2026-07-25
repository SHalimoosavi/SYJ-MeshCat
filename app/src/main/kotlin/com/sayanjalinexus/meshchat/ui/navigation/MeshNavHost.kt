package com.sayanjalinexus.meshchat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.sayanjalinexus.meshchat.ui.home.HomeScreen
import com.sayanjalinexus.meshchat.ui.theme.MeshChatTheme

/**
 * Root navigation graph. Grows one destination per feature milestone;
 * currently just [MeshRoute.Home].
 */
@Composable
fun MeshNavHost(
    navController: NavHostController,
    startDestination: MeshRoute = MeshRoute.Home,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<MeshRoute.Home> {
            HomeScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MeshNavHostPreview() {
    MeshChatTheme {
        MeshNavHost(navController = rememberNavController())
    }
}
