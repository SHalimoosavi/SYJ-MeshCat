package com.sayanjalinexus.meshchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sayanjalinexus.meshchat.ui.navigation.MeshNavHost
import com.sayanjalinexus.meshchat.ui.theme.MeshChatTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the entire app. All screens are Compose
 * destinations reached through [MeshNavHost]; this class owns no UI logic
 * of its own.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MeshChatApp()
        }
    }
}

@Composable
private fun MeshChatApp() {
    MeshChatTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            MeshNavHost(navController = navController)
        }
    }
}
