package dev.itsvic.parceltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.room.Room
import dev.itsvic.parceltracker.api.Service
import dev.itsvic.parceltracker.api.Parcel as APIParcel
import dev.itsvic.parceltracker.db.AppDatabase
import dev.itsvic.parceltracker.ui.theme.ParcelTrackerTheme
import dev.itsvic.parceltracker.ui.views.AddParcelView
import dev.itsvic.parceltracker.ui.views.HomeView
import dev.itsvic.parceltracker.ui.views.ParcelView
import kotlinx.coroutines.Dispatchers
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
                onNavigateToParcel = { navController.navigate(route = ParcelPage(it.id)) },
            )
        }
        composable<ParcelPage> { backStackEntry ->
            val route: ParcelPage = backStackEntry.toRoute()
            val parcelDb = db.parcelDao().getById(route.parcelDbId).collectAsState(null)

            // TODO: fetch APIParcel
            ParcelView(
                APIParcel(parcelDb.value?.parcelId ?: "", emptyList(), "Placeholder"),
                parcelDb.value?.humanName ?: "",
                parcelDb.value?.service ?: Service.UNDEFINED,
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable<AddParcelPage> {
            var addFinished by remember { mutableStateOf(Pair(false, 0)) }

            AddParcelView(
                onBackPressed = { navController.popBackStack() },
                onCompleted = {
                    scope.launch(Dispatchers.IO) {
                        val id = db.parcelDao().insert(it)
                        addFinished = Pair(true, id.toInt())
                    }
                },
            )

            LaunchedEffect(addFinished) {
                if (addFinished.first) {
                    navController.navigate(route = ParcelPage(addFinished.second)) {
                        popUpTo(HomePage)
                    }
                }
            }
        }
    }
}
