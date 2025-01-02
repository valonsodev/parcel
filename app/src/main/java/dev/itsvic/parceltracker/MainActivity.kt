package dev.itsvic.parceltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import dev.itsvic.parceltracker.db.AppDatabase
import dev.itsvic.parceltracker.ui.theme.ParcelTrackerTheme
import dev.itsvic.parceltracker.ui.views.AddParcelView
import dev.itsvic.parceltracker.ui.views.HomeView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "parcel-tracker")
            .build()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParcelTrackerTheme {
                Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
                    ParcelAppNavigation(db)
                }
            }
        }
    }
}

@Serializable
object HomePage

@Serializable
data class ParcelPage(val parcelDbId: Int)

@Serializable
object AddParcelPage

@Composable
fun ParcelAppNavigation(db: AppDatabase) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = HomePage,
    ) {
        composable<HomePage> {
            val parcels = db.parcelDao().getAll().collectAsState(initial = emptyList())

            HomeView(
                parcels = parcels.value,
                onNavigateToAddParcel = { navController.navigate(route = AddParcelPage) },
                onNavigateToParcel = {},
            )
        }
        composable<ParcelPage> { }
        composable<AddParcelPage> {
            AddParcelView(
                onBackPressed = { navController.popBackStack() },
                onCompleted = {
                    scope.launch(Dispatchers.IO) {
                        val id = db.parcelDao().insert(it)
                        navController.navigate(route = ParcelPage(id.toInt())) {
                            popUpTo(HomePage)
                        }
                    }
                },
            )
        }
    }
}
