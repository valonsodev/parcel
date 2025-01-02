package dev.itsvic.parceltracker.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.itsvic.parceltracker.api.Service
import dev.itsvic.parceltracker.db.Parcel
import dev.itsvic.parceltracker.ui.theme.ParcelTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParcelView(
    onBackPressed: () -> Unit,
    onCompleted: (Parcel) -> Unit,
) {
    var humanName by remember { mutableStateOf("") }
    var trackingId by remember { mutableStateOf("") }
    var needsPostalCode by remember { mutableStateOf(false) }
    var postalCode by remember { mutableStateOf("") }
    var service by remember { mutableStateOf(Service.UNDEFINED) }
    var expanded by remember { mutableStateOf(false) }

    val serviceOptions = listOf(
        Service.DHL,
        Service.GLS,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add a parcel")
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = humanName,
                    onValueChange = { humanName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = trackingId,
                    onValueChange = { trackingId = it },
                    label = { Text("Tracking ID") },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Service dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = if (service == Service.UNDEFINED) "" else service.toString(),
                        onValueChange = {},
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        readOnly = true,
                        label = { Text("Delivery service") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        serviceOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.toString()) },
                                onClick = {
                                    service = option
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Specify a postal code")
                        // TODO: cleaner line breaks how?
                        Text(
                            "Some parcels require a postal code to\nview full details.",
                            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Checkbox(
                        checked = needsPostalCode,
                        onCheckedChange = { needsPostalCode = it }
                    )
                }

                if (needsPostalCode)
                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = { postalCode = it },
                        label = { Text("Postal code") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = {
                        onCompleted(
                            Parcel(
                                humanName = humanName,
                                parcelId = trackingId,
                                service = service,
                                postalCode = if (needsPostalCode) postalCode else null
                            )
                        )
                    }) {
                        Text("Add parcel")
                    }
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
fun AddParcelViewPreview() {
    ParcelTrackerTheme {
        AddParcelView(
            onBackPressed = {},
            onCompleted = {},
        )
    }
}
