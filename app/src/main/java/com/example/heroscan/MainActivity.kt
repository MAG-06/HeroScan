package com.example.heroscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.heroscan.ui.detail.ComicDetailScreen
import com.example.heroscan.ui.home.HomeScreen
import com.example.heroscan.ui.theme.HeroScanTheme
import com.example.heroscan.ui.scan.ScanScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HeroScanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HeroScanNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HeroScanNavHost(modifier: Modifier = Modifier) {

    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "home", modifier = modifier) {

        composable("home") {
            HomeScreen(
                onScanClick = { navController.navigate("scan") },
                onComicClick = { comicId -> navController.navigate("comicDetail/$comicId")}
            )
        }

        composable("comicDetail/{comicId}") { backStackEntry ->
            val comicId = backStackEntry.arguments?.getString("comicId") ?: ""
            ComicDetailScreen(
                comicId = comicId,
                onBackClick = { navController.popBackStack() },
                onScanAnotherClick = { navController.navigate("scan") }
            )
        }

        composable("scan") {
            ScanScreen(onBackClick = { navController.popBackStack() } )
        }

    }
}