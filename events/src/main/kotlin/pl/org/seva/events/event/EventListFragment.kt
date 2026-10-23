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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import pl.org.seva.events.R
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.login.Login
import pl.org.seva.events.main.extension.nav
import pl.org.seva.events.main.extension.plus
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.EventsTheme
import pl.org.seva.events.main.ui.composeContent

class EventListFragment : Fragment() {

    private val comms by instance<Comms>()
    private val events by instance<Events>()
    private val login by instance<Login>()

    private var eventItems by mutableStateOf(emptyList<Event>())
    private var showAddFab by mutableStateOf(false)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ) = composeContent {
        EventListScreen(
                events = eventItems,
                showAddFab = showAddFab,
                onEventClick = { position ->
                    eventViewModel.value.withPosition(position)
                    nav(R.id.action_eventsFragment_to_eventDetailsFragment)
                },
                onAddEventClick = {
                    nav(R.id.action_eventsFragment_to_createEventFragment)
                },
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.event_list, menu)
                menu.findItem(R.id.action_login).isVisible = !login.isLoggedIn
                menu.findItem(R.id.action_log_out).isVisible = login.isLoggedIn
            }

            override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
                R.id.action_login -> nav(R.id.action_eventsFragment_to_loginConfirmationFragment)
                R.id.action_log_out -> nav(R.id.action_eventsFragment_to_logOutConfirmationFragment)
                R.id.action_comms -> nav(R.id.action_eventsFragment_to_commListFragment)
                R.id.action_system_messages -> nav(R.id.action_eventsFragment_to_systemMessagesFragment)
                R.id.action_about -> nav(R.id.action_eventsFragment_to_aboutFragment)
                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        refreshComms()
        refreshEvents()

        (comms.updatedLiveData() + this) {
            refreshComms()
        }

        (events.updatedLiveData() + this) {
            refreshEvents()
        }
    }

    private fun refreshComms() {
        showAddFab = comms.isAdminOfAny
    }

    private fun refreshEvents() {
        eventItems = (0 until events.size).map { events[it] }
    }
}

/**
 * Jetpack Compose screen replacing `res/layout/fr_event_list.xml`.
 */
@Composable
fun EventListScreen(
        events: List<Event>,
        showAddFab: Boolean,
        onEventClick: (Int) -> Unit,
        onAddEventClick: () -> Unit,
) {
    Scaffold(
            floatingActionButton = {
                if (showAddFab) {
                    FloatingActionButton(onClick = onAddEventClick) {
                        Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(id = R.string.create_event_name),
                        )
                    }
                }
            },
    ) { innerPadding ->
        if (events.isEmpty()) {
            Box(
                    modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment = Alignment.Center,
            ) {
                Text(
                        text = stringResource(id = R.string.events_list_no_events),
                        fontSize = 20.sp,
                )
            }
        } else {
            LazyColumn(
                    modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
            ) {
                itemsIndexed(items = events) { index, event ->
                    EventRow(
                            name = event.name,
                            time = event.time.toString(),
                            onClick = { onEventClick(index) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventListScreenPreview() {
    EventsTheme {
        EventListScreen(
                events = emptyList(),
                showAddFab = true,
                onEventClick = {},
                onAddEventClick = {},
        )
    }
}
