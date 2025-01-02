package dev.itsvic.parceltracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.room.Room
import dev.itsvic.parceltracker.api.Service
import dev.itsvic.parceltracker.api.getParcel
import dev.itsvic.parceltracker.api.Parcel as APIParcel
import dev.itsvic.parceltracker.db.AppDatabase
import dev.itsvic.parceltracker.db.Parcel
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
            var apiParcel: APIParcel? by remember { mutableStateOf(null) }

            LaunchedEffect(parcelDb.value) {
                if (parcelDb.value != null) {
                    launch(Dispatchers.IO) {
                        apiParcel = getParcel(
                            parcelDb.value!!.parcelId,
                            parcelDb.value!!.postalCode,
                            parcelDb.value!!.service
                        )
                        Log.i("MainActivity", "DB: $parcelDb, API: $apiParcel")
                    }
                }
            }

            if (apiParcel == null)
                Box(
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.background)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            else
                ParcelView(
                    apiParcel!!,
                    parcelDb.value!!.humanName,
                    parcelDb.value!!.service,
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
