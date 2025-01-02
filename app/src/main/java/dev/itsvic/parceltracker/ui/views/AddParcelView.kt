package dev.itsvic.parceltracker.ui.views

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.itsvic.parceltracker.R
import dev.itsvic.parceltracker.api.Service
import dev.itsvic.parceltracker.api.serviceOptions
import dev.itsvic.parceltracker.api.serviceToHumanString
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.add_a_parcel))
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
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = humanName,
                    onValueChange = { humanName = it },
                    label = { Text(stringResource(R.string.parcel_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = trackingId,
                    onValueChange = { trackingId = it },
                    label = { Text(stringResource(R.string.tracking_id)) },
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
                        label = { Text(stringResource(R.string.delivery_service)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        serviceOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(stringResource(serviceToHumanString[option]!!)) },
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
                    Column(modifier = Modifier.fillMaxWidth(0.8f)) {
                        Text(stringResource(R.string.specify_a_postal_code))
                        // TODO: cleaner line breaks how?
                        Text(
                            stringResource(R.string.specify_postal_code_flavor_text),
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Checkbox(
                        checked = needsPostalCode,
                        onCheckedChange = { needsPostalCode = it },
                    )
                }

                AnimatedVisibility(needsPostalCode) {
                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = { postalCode = it },
                        label = { Text(stringResource(R.string.postal_code)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

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
                        Text(stringResource(R.string.add_parcel))
                    }
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
@Preview(locale = "pl", name = "Polish")
fun AddParcelViewPreview() {
    ParcelTrackerTheme {
        AddParcelView(
            onBackPressed = {},
            onCompleted = {},
        )
    }
}
