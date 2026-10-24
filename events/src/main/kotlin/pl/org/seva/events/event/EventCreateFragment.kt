/*
 * Copyright (C) 2017 Wiktor Nizio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you like this program, consider donating bitcoin: bc1qncxh5xs6erq6w4qz3a7xl7f50agrgn3w58dsfp
 */

package pl.org.seva.events.event

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.GoogleMap
import pl.org.seva.events.R
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.main.extension.question
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.EventsTheme
import pl.org.seva.events.main.ui.GoogleMapView
import pl.org.seva.events.main.ui.MapHolder
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun EventCreateRoute(
        vm: EventCreateViewModel,
        commError: Boolean,
        nameError: Boolean,
        timeError: Boolean,
        dateError: Boolean,
        onClearCommError: () -> Unit,
        onClearNameError: () -> Unit,
        onClearTimeError: () -> Unit,
        onClearDateError: () -> Unit,
        onNavigateToLocationPicker: () -> Unit,
        onBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val comms by instance<Comms>()

    val comm by vm.comm.collectAsStateWithLifecycle()
    val name by vm.name.collectAsStateWithLifecycle()
    val time by vm.time.collectAsStateWithLifecycle()
    val date by vm.date.collectAsStateWithLifecycle()
    val desc by vm.desc.collectAsStateWithLifecycle()
    val location by vm.location.collectAsStateWithLifecycle()

    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val adminComms = remember { comms.namesIsAdminOf }

    LaunchedEffect(adminComms) {
        if (adminComms.isEmpty()) {
            onBack()
        } else if (vm.comm.value.isNullOrEmpty()) {
            vm.comm.value = adminComms[0]
        }
    }

    val mapHolder = remember { MapHolder() }
    val liveMap by mapHolder.liveMap.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            liveMap?.isMyLocationEnabled = true
        }
    }

    LaunchedEffect(liveMap, location) {
        val map = liveMap ?: return@LaunchedEffect
        mapHolder.markPosition(location?.location)
        if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    DisposableEffect(lifecycle, liveMap) {
        val observer = LifecycleEventObserver { _, lifecycleEvent ->
            if ((lifecycleEvent == Lifecycle.Event.ON_RESUME) &&
                    (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED)
            ) {
                liveMap?.isMyLocationEnabled = true
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    BackHandler {
        if (vm.isFilledIn) {
            context.question(
                    message = context.getString(R.string.main_activity_dismiss_event),
                    yes = {
                        vm.clear()
                        onBack()
                    },
            )
        } else {
            onBack()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = date
                        ?.atStartOfDay(ZoneOffset.UTC)
                        ?.toInstant()
                        ?.toEpochMilli(),
        )
        DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    vm.date.value = Instant.ofEpochMilli(millis)
                                            .atZone(ZoneOffset.UTC)
                                            .toLocalDate()
                                }
                                showDatePicker = false
                            },
                    ) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val initialTime = time ?: LocalTime.now()
        val timePickerState = rememberTimePickerState(
                initialHour = initialTime.hour,
                initialMinute = initialTime.minute,
                is24Hour = true,
        )
        AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(
                            onClick = {
                                vm.time.value = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                showTimePicker = false
                            },
                    ) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                },
                text = {
                    TimePicker(state = timePickerState)
                },
        )
    }

    EventCreateScreen(
            showCommSelector = adminComms.size > 1,
            comm = comm.orEmpty(),
            commError = commError,
            onCommClick = {
                AlertDialog.Builder(context)
                        .setItems(adminComms) { dialog, which ->
                            vm.comm.value = adminComms[which]
                            onClearCommError()
                            dialog.dismiss()
                        }
                        .show()
            },
            name = name.orEmpty(),
            nameError = nameError,
            onNameChange = {
                vm.name.value = it
                onClearNameError()
            },
            time = time?.toString().orEmpty(),
            timeError = timeError,
            onTimeClick = {
                onClearTimeError()
                showTimePicker = true
            },
            date = date?.toString().orEmpty(),
            dateError = dateError,
            onDateClick = {
                onClearDateError()
                showDatePicker = true
            },
            desc = desc.orEmpty(),
            onDescChange = { vm.desc.value = it },
            address = location?.address.orEmpty(),
            onLocationClick = onNavigateToLocationPicker,
            hasLocation = location != null,
            onMapReady = { map -> mapHolder withMap map },
    )
}

private fun Modifier.clickableReadOnlyField(onClick: () -> Unit): Modifier =
        pointerInput(onClick) {
            awaitEachGesture {
                awaitFirstDown(pass = PointerEventPass.Initial)
                val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                if (up != null) {
                    onClick()
                }
            }
        }

@Composable
fun EventCreateScreen(
        showCommSelector: Boolean,
        comm: String,
        commError: Boolean,
        onCommClick: () -> Unit,
        name: String,
        nameError: Boolean,
        onNameChange: (String) -> Unit,
        time: String,
        timeError: Boolean,
        onTimeClick: () -> Unit,
        date: String,
        dateError: Boolean,
        onDateClick: () -> Unit,
        desc: String,
        onDescChange: (String) -> Unit,
        address: String,
        onLocationClick: () -> Unit,
        hasLocation: Boolean,
        onMapReady: (GoogleMap) -> Unit,
) {
    val emptyErrorText = stringResource(id = R.string.create_event_cant_be_empty)

    Column(modifier = Modifier.fillMaxSize()) {
        if (showCommSelector) {
            OutlinedTextField(
                    value = comm,
                    onValueChange = {},
                    readOnly = true,
                    isError = commError,
                    supportingText = if (commError) ({ Text(text = emptyErrorText) }) else null,
                    label = { Text(text = stringResource(id = R.string.create_event_comm)) },
                    modifier = Modifier
                            .fillMaxWidth()
                            .clickableReadOnlyField(onCommClick),
            )
        }
        OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                isError = nameError,
                supportingText = if (nameError) ({ Text(text = emptyErrorText) }) else null,
                label = { Text(text = stringResource(id = R.string.create_event_name)) },
                modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
                value = time,
                onValueChange = {},
                readOnly = true,
                isError = timeError,
                supportingText = if (timeError) ({ Text(text = emptyErrorText) }) else null,
                label = { Text(text = stringResource(id = R.string.create_event_time)) },
                modifier = Modifier
                        .fillMaxWidth()
                        .clickableReadOnlyField(onTimeClick),
        )
        OutlinedTextField(
                value = date,
                onValueChange = {},
                readOnly = true,
                isError = dateError,
                supportingText = if (dateError) ({ Text(text = emptyErrorText) }) else null,
                label = { Text(text = stringResource(id = R.string.create_event_date)) },
                modifier = Modifier
                        .fillMaxWidth()
                        .clickableReadOnlyField(onDateClick),
        )
        OutlinedTextField(
                value = desc,
                onValueChange = onDescChange,
                label = { Text(text = stringResource(id = R.string.create_event_desc)) },
                modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
                value = address,
                onValueChange = {},
                readOnly = true,
                label = { Text(text = stringResource(id = R.string.create_event_location)) },
                modifier = Modifier
                        .fillMaxWidth()
                        .clickableReadOnlyField(onLocationClick),
        )
        if (hasLocation && !LocalInspectionMode.current) {
            GoogleMapView(
                    modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    onMapReady = onMapReady,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventCreateScreenPreview() {
    EventsTheme {
        EventCreateScreen(
                showCommSelector = true,
                comm = "Sample Community",
                commError = false,
                onCommClick = {},
                name = "Sample Event",
                nameError = false,
                onNameChange = {},
                time = "18:00",
                timeError = false,
                onTimeClick = {},
                date = "2026-10-24",
                dateError = false,
                onDateClick = {},
                desc = "",
                onDescChange = {},
                address = "",
                onLocationClick = {},
                hasLocation = false,
                onMapReady = {},
        )
    }
}
