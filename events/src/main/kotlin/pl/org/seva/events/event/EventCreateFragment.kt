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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.launch
import pl.org.seva.events.R
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.main.data.Permissions
import pl.org.seva.events.main.extension.back
import pl.org.seva.events.main.extension.createMapHolder
import pl.org.seva.events.main.extension.enableMyLocationOnResume
import pl.org.seva.events.main.extension.nav
import pl.org.seva.events.main.extension.onBack
import pl.org.seva.events.main.extension.plus
import pl.org.seva.events.main.extension.question
import pl.org.seva.events.main.extension.requestLocationPermission
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.EventsTheme
import pl.org.seva.events.main.ui.GoogleMapView
import pl.org.seva.events.main.ui.composeContent
import java.time.LocalDateTime

class EventCreateFragment : Fragment() {

    private val events by instance<Events>()
    private val comms by instance<Comms>()
    private val permissions by instance<Permissions>()

    private val vm by eventCreateViewModel

    private var commError by mutableStateOf(false)
    private var nameError by mutableStateOf(false)
    private var timeError by mutableStateOf(false)
    private var dateError by mutableStateOf(false)

    private val mapHolder by lazy {
        createMapHolder {
            requestLocationPermission = this@EventCreateFragment::requestLocationPermission
        }.also { holder ->
            (holder.liveMap + this) { map ->
                enableMyLocationOnResume(map)
                (vm.location + this) { holder.markPosition(it?.location) }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ) = composeContent {
        val comm by vm.comm.observeAsState()
        val name by vm.name.observeAsState()
        val time by vm.time.observeAsState()
        val date by vm.date.observeAsState()
        val desc by vm.desc.observeAsState()
        val location by vm.location.observeAsState()

        val adminComms = comms.namesIsAdminOf

        EventCreateScreen(
                showCommSelector = adminComms.size > 1,
                comm = comm.orEmpty(),
                commError = commError,
                onCommClick = {
                    AlertDialog.Builder(requireContext())
                            .setItems(adminComms) { dialog, which ->
                                vm.comm.value = adminComms[which]
                                commError = false
                                dialog.dismiss()
                            }
                            .show()
                },
                name = name.orEmpty(),
                nameError = nameError,
                onNameChange = {
                    vm.name.value = it
                    nameError = false
                },
                time = time?.toString().orEmpty(),
                timeError = timeError,
                onTimeClick = {
                    timeError = false
                    nav(R.id.action_eventCreateFragment_to_timePicker)
                },
                date = date?.toString().orEmpty(),
                dateError = dateError,
                onDateClick = {
                    dateError = false
                    nav(R.id.action_eventCreateFragment_to_datePicker)
                },
                desc = desc.orEmpty(),
                onDescChange = { vm.desc.value = it },
                address = location?.address.orEmpty(),
                onLocationClick = {
                    nav(R.id.action_createEventFragment_to_locationPickerFragment)
                },
                hasLocation = location != null,
                onMapReady = { map -> mapHolder withMap map },
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.event_create, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
                R.id.action_ok -> {
                    confirm()
                    true
                }
                android.R.id.home -> {
                    onBackOrHomePressed()
                    true
                }
                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val adminComms = comms.namesIsAdminOf
        if (adminComms.isEmpty()) {
            back()
        } else if (vm.comm.value.isNullOrEmpty()) {
            vm.comm.value = adminComms[0]
        }

        onBack { onBackOrHomePressed() }
    }

    private fun confirm() {
        commError = vm.comm.value.isNullOrEmpty()
        nameError = vm.name.value.isNullOrEmpty()
        timeError = vm.time.value == null
        dateError = vm.date.value == null

        if (commError || nameError || timeError || dateError) return

        lifecycleScope.launch {
            events.add(Event(
                    comm = checkNotNull(vm.comm.value),
                    name = checkNotNull(vm.name.value),
                    time = LocalDateTime.of(checkNotNull(vm.date.value), checkNotNull(vm.time.value)),
                    location = vm.location.value?.location,
                    address = vm.location.value?.address,
                    desc = vm.desc.value,
            ))
            vm.clear()
            back()
        }
    }

    private fun onBackOrHomePressed() {
        if (vm.isFilledIn) {
            question(
                    message = getString(R.string.main_activity_dismiss_event),
                    yes = {
                        vm.clear()
                        back()
                    },
            )
        } else {
            back()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
            requestCode: Int,
            requests: Array<String>,
            grantResults: IntArray,
    ) = permissions.onRequestPermissionsResult(this, requestCode, requests, grantResults)
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

/**
 * Jetpack Compose screen replacing `res/layout/fr_event_create.xml`.
 */
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
