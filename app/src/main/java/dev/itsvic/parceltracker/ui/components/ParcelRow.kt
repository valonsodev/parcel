package dev.itsvic.parceltracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.itsvic.parceltracker.api.Service
import dev.itsvic.parceltracker.db.Parcel
import dev.itsvic.parceltracker.ui.theme.ParcelTrackerTheme

// TODO: show the parcel status. requires a fetch so not doing it yet
@Composable
fun ParcelRow(parcel: Parcel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp, 12.dp)) {
            Text(parcel.humanName, color = MaterialTheme.colorScheme.onBackground)

            Text(
                "${parcel.service}: ${parcel.parcelId}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
@PreviewLightDark
fun ParcelRowPreview() {
    ParcelTrackerTheme {
        Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
            ParcelRow(
                Parcel(0, "My precious package", "EXMPL0001", null, Service.EXAMPLE),
                onClick = {},
            )
        }
    }
}
