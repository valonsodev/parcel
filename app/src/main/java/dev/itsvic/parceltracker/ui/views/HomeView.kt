package dev.itsvic.parceltracker.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.itsvic.parceltracker.ui.theme.ParcelTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    onNavigateToAddParcel: () -> Unit,
) {
    Scaffold(
        topBar = {
            LargeTopAppBar(title = {
                Text("Parcel")
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddParcel) {
                Icon(Icons.Default.Add, contentDescription = "Add parcel")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(horizontal = 16.dp)) {
            Text("Hello")
        }
    }
}

@Composable
@PreviewLightDark
fun HomeViewPreview() {
    ParcelTrackerTheme {
        HomeView(
            onNavigateToAddParcel = {}
        )
    }
}
