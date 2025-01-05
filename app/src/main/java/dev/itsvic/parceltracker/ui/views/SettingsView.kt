package dev.itsvic.parceltracker.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import dev.itsvic.parceltracker.R
import dev.itsvic.parceltracker.ui.theme.ParcelTrackerTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import dev.itsvic.parceltracker.BuildConfig
import dev.itsvic.parceltracker.DEMO_MODE
import dev.itsvic.parceltracker.api.ParcelHistoryItem
import dev.itsvic.parceltracker.api.Service
import dev.itsvic.parceltracker.api.Status
import dev.itsvic.parceltracker.dataStore
import dev.itsvic.parceltracker.db.Parcel
import dev.itsvic.parceltracker.sendNotification
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    onBackPressed: () -> Unit,
) {
    val context = LocalContext.current
    val demoMode = context.dataStore.data.map { it[DEMO_MODE] ?: false }.collectAsState(false)
    val coroutineScope = rememberCoroutineScope()

    val setDemoMode: (Boolean) -> Unit = { value ->
        coroutineScope.launch {
            context.dataStore.edit { it[DEMO_MODE] = value }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(stringResource(R.string.settings))
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.go_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            Text(
                stringResource(R.string.settings_experimental),
                modifier = Modifier.padding(16.dp, 2.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier
                    .clickable { setDemoMode(demoMode.value.not()) }
                    .padding(16.dp, 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.8f)) {
                    Text(stringResource(R.string.demo_mode))
                    Text(
                        stringResource(R.string.demo_mode_detail),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Switch(checked = demoMode.value, onCheckedChange = { setDemoMode(it) })
            }

            FilledTonalButton(
                onClick = {
                    context.sendNotification(
                        Parcel(0xf100f, "Cool stuff", "", null, Service.EXAMPLE),
                        Status.OutForDelivery,
                        ParcelHistoryItem(
                            "The courier has picked up the package",
                            LocalDateTime.now(),
                            ""
                        )
                    )
                },
                modifier = Modifier
                    .padding(16.dp, 12.dp)
                    .fillMaxWidth()
            ) {
                Text("Send test notification")
            }

            Text(
                "Parcel ${BuildConfig.VERSION_NAME}",
                modifier = Modifier.padding(16.dp, 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
@PreviewLightDark
private fun SettingsViewPreview() {
    ParcelTrackerTheme {
        SettingsView(
            onBackPressed = {},
        )
    }
}
