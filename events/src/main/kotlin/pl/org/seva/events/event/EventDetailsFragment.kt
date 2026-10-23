/*
 * Copyright (C) 2019 Wiktor Nizio
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
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import pl.org.seva.events.R
import pl.org.seva.events.main.data.Permissions
import pl.org.seva.events.main.extension.createMapHolder
import pl.org.seva.events.main.extension.enableMyLocationOnResume
import pl.org.seva.events.main.extension.plus
import pl.org.seva.events.main.extension.requestLocationPermission
import pl.org.seva.events.main.extension.title
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.EventsTheme
import pl.org.seva.events.main.ui.GoogleMapView
import pl.org.seva.events.main.ui.composeContent

class EventDetailsFragment : Fragment() {

    private val permissions by instance<Permissions>()

    private val mapHolder by lazy {
        createMapHolder {
            requestLocationPermission = this@EventDetailsFragment::requestLocationPermission
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ) = composeContent {
        val event = eventViewModel.value.event
        EventDetailsScreen(
                comm = event.comm,
                name = event.name,
                time = event.time.toLocalTime().toString(),
                date = event.time.toLocalDate().toString(),
                desc = event.desc,
                address = event.address,
                hasLocation = event.location != null,
                onMapReady = { map -> mapHolder withMap map },
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val event = eventViewModel.value.event
        title = event.name
        if (event.location != null) {
            (mapHolder.liveMap + this) { map ->
                mapHolder.markPosition(event.location)
                enableMyLocationOnResume(map)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
            requestCode: Int,
            requests: Array<String>,
            grantResults: IntArray,
    ) = permissions.onRequestPermissionsResult(this, requestCode, requests, grantResults)
}

/**
 * Jetpack Compose screen replacing `res/layout/fr_event_details.xml`.
 */
@Composable
fun EventDetailsScreen(
        comm: String,
        name: String,
        time: String,
        date: String,
        desc: String?,
        address: String?,
        hasLocation: Boolean,
        onMapReady: (GoogleMap) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
                value = comm,
                onValueChange = {},
                readOnly = true,
                label = { Text(text = stringResource(id = R.string.event_details_comm)) },
                modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
                value = name,
                onValueChange = {},
                readOnly = true,
                label = { Text(text = stringResource(id = R.string.event_details_name)) },
                modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
                value = time,
                onValueChange = {},
                readOnly = true,
                label = { Text(text = stringResource(id = R.string.event_details_time)) },
                modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
                value = date,
                onValueChange = {},
                readOnly = true,
                label = { Text(text = stringResource(id = R.string.event_details_date)) },
                modifier = Modifier.fillMaxWidth(),
        )
        if (!desc.isNullOrEmpty()) {
            OutlinedTextField(
                    value = desc,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(id = R.string.event_details_desc)) },
                    modifier = Modifier.fillMaxWidth(),
            )
        }
        if (!address.isNullOrEmpty()) {
            OutlinedTextField(
                    value = address,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(id = R.string.event_details_location)) },
                    modifier = Modifier.fillMaxWidth(),
            )
        }
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
fun EventDetailsScreenPreview() {
    EventsTheme {
        EventDetailsScreen(
                comm = "Sample Community",
                name = "Sample Event",
                time = "18:00",
                date = "2026-10-24",
                desc = "Sample description",
                address = "Main Street 1",
                hasLocation = false,
                onMapReady = {},
        )
    }
}
