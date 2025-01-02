package dev.itsvic.parceltracker.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.itsvic.parceltracker.api.Parcel
import dev.itsvic.parceltracker.api.ParcelHistoryItem
import dev.itsvic.parceltracker.api.Service
import dev.itsvic.parceltracker.ui.theme.ParcelTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelView(parcel: Parcel, humanName: String, service: Service) {
    Scaffold(topBar = {
        MediumTopAppBar(title = {
            Text(humanName)
        })
    }) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp, 0.dp)) {
            Row(
                modifier = Modifier.padding(bottom = 14.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    service.toString(),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    parcel.id,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                "Current status",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                parcel.currentStatus,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(parcel.history[0].description)

            Text(
                "Package history",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(parcel.history) { item ->
                    ParcelHistoryItemView(item)
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
private fun ParcelViewPreview() {
    val parcel = Parcel(
        "EXMPL0001",
        listOf(
            ParcelHistoryItem(
                "The package got lost. Whoops!",
                "2025-01-01 12:00:00",
                "Warsaw, Poland"
            ),
            ParcelHistoryItem(
                "Arrived at local warehouse",
                "2025-01-01 10:00:00",
                "Warsaw, Poland"
            ),
            ParcelHistoryItem(
                "En route to local warehouse",
                "2024-12-01 12:00:00",
                "Netherlands"
            ),
            ParcelHistoryItem(
                "Label created",
                "2024-12-01 12:00:00",
                "Netherlands"
            ),
        ),
        "Lost"
    )
    ParcelTrackerTheme {
        ParcelView(parcel, "My precious package", Service.EXAMPLE)
    }
}
