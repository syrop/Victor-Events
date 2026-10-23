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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.gms.maps.GoogleMap
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.back
import pl.org.seva.events.main.extension.createInteractiveMapHolder
import pl.org.seva.events.main.extension.enableMyLocationOnResume
import pl.org.seva.events.main.extension.plus
import pl.org.seva.events.main.extension.prefs
import pl.org.seva.events.main.ui.EventsTheme
import pl.org.seva.events.main.ui.GoogleMapView
import pl.org.seva.events.main.ui.composeContent

class LocationPickerFragment : Fragment() {

    private val mapHolder by lazy {
        createInteractiveMapHolder {
            prefs = prefs(SHARED_PREFERENCES_TAG)
            onLocationSet = { eventCreateViewModel.value.location.value = it }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ) = composeContent {
        val viewModel = eventCreateViewModel.value
        val location by viewModel.location.observeAsState()

        LocationPickerScreen(
                address = location?.address.orEmpty(),
                hasLocation = location != null,
                onDeleteLocationClick = { viewModel.location.value = null },
                onMapReady = { map -> mapHolder withMap map },
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.location_picker, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
                R.id.action_ok -> back()
                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val viewModel = eventCreateViewModel.value
        (mapHolder.liveMap + this) { map ->
            enableMyLocationOnResume(map)
            (viewModel.location + this@LocationPickerFragment) {
                mapHolder.markPosition(it?.location)
            }
        }
    }

    companion object {
        private const val SHARED_PREFERENCES_TAG = "fragment_location_picker_preferences"
    }
}

/**
 * Jetpack Compose screen replacing `res/layout/fr_location_picker.xml`.
 */
@Composable
fun LocationPickerScreen(
        address: String,
        hasLocation: Boolean,
        onDeleteLocationClick: () -> Unit,
        onMapReady: (GoogleMap) -> Unit,
) {
    Scaffold(
            floatingActionButton = {
                if (hasLocation) {
                    FloatingActionButton(onClick = onDeleteLocationClick) {
                        Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.pick_location_label),
                        )
                    }
                }
            },
    ) { innerPadding ->
        Column(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
        ) {
            OutlinedTextField(
                    value = address,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(id = R.string.pick_location_select_location)) },
                    modifier = Modifier.fillMaxWidth(),
            )
            if (!LocalInspectionMode.current) {
                GoogleMapView(
                        modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        onMapReady = onMapReady,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationPickerScreenPreview() {
    EventsTheme {
        LocationPickerScreen(
                address = "Main Street 1",
                hasLocation = true,
                onDeleteLocationClick = {},
                onMapReady = {},
        )
    }
}
