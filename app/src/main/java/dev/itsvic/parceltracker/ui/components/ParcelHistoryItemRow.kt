package dev.itsvic.parceltracker.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.sp
import dev.itsvic.parceltracker.api.ParcelHistoryItem
import dev.itsvic.parceltracker.ui.theme.ParcelTrackerTheme

@Composable
fun ParcelHistoryItemRow(item: ParcelHistoryItem) {
    Column {
        Text(
            item.description,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(item.time, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                item.location,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
@PreviewLightDark
private fun ParcelHistoryItemRowPreview() {
    val exampleItem = ParcelHistoryItem(
        "The package got lost. Whoops!",
        "2025-01-01 12:00:00",
        "Warsaw, Poland"
    )

    ParcelTrackerTheme {
        Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
            ParcelHistoryItemRow(
                exampleItem
            )
        }
    }
}
