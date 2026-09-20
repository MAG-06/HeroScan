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
import com.example.heroscan.model.Comic
import com.example.heroscan.ui.home.HomeScreen
import com.example.heroscan.ui.theme.HeroScanTheme
import com.example.heroscan.ui.scan.CoverScanScreen
import com.example.heroscan.ui.scan.ScanScreen
import com.example.heroscan.ui.search.SearchScreen
import com.example.heroscan.ui.detail.ComicDetailScreen
import com.google.gson.Gson
import java.net.URLEncoder
import java.net.URLDecoder


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
    val gson = Gson()


    NavHost(navController = navController, startDestination = "home", modifier = modifier) {

        composable("home") {
            HomeScreen(
                onScanClick = { navController.navigate("scan") },
                onComicClick = { comicId -> navController.navigate("comicDetail/$comicId")},
                onPortadaClick = { navController.navigate("coverScan") },
                onTextoClick = { navController.navigate("search") }
            )
        }



        composable("scan") {
            ScanScreen(onBackClick = { navController.popBackStack() } )
        }

        composable("coverScan") {
            CoverScanScreen(onBackClick = { navController.popBackStack() })
        }

        composable("search") {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onComicFound = { comic ->
                    val comicJson = URLEncoder.encode(gson.toJson(comic), "UTF-8")
                    navController.navigate("comicDetail/$comicJson")
                }
            )
        }

        composable("comicDetail/{comicJson}") { backStackEntry ->
            val comicJson = URLDecoder.decode(
                backStackEntry.arguments?.getString("comicJson") ?: "",
                "UTF-8"
            )
            val comic = gson.fromJson(comicJson, Comic::class.java)

            ComicDetailScreen(
                comic = comic,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}