package dev.itsvic.parceltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.itsvic.parceltracker.ui.theme.ParcelTrackerTheme
import dev.itsvic.parceltracker.ui.views.HomeView
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParcelTrackerTheme {
                ParcelAppNavigation()
            }
        }
    }
}

@Serializable
object HomePage
@Serializable
object ParcelPage
@Serializable
object AddParcelPage

@Composable
fun ParcelAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HomePage) {
        composable<HomePage> { HomeView(
            onNavigateToAddParcel = { navController.navigate(route = AddParcelPage) }
        ) }
        composable<ParcelPage> {  }
        composable<AddParcelPage> {  }
    }
}
