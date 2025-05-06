package com.audio.test

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.audio.test.theme.TestComposeTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestComposeTheme {
                AppNavigation()
            }
        }
    }


}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "screenA") {
        composable("screenA") {
            ScreenA(navController)
        }
        composable(
            "screenB"
            //arguments = listOf(navArgument("rawText") { type = NavType.StringType })
        ) {
            ScreenB(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenA(nav: NavHostController ) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pantalla A") })
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Text("A content")
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                nav.navigate("screenB")
            }) {
                Text("Process", style = MaterialTheme.typography.titleMedium)
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenB(nav: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pantalla b") })
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Text("b content")
        }
    }
}





