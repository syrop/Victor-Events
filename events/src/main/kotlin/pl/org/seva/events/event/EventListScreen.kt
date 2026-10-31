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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.org.seva.events.R
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.EventsTheme

@Composable
fun EventListRoute(
        eventViewModel: EventViewModel,
        onNavigateToEventDetails: () -> Unit,
        onNavigateToCreateEvent: () -> Unit,
) {
    val comms by instance<Comms>()
    val events by instance<Events>()

    val showAddFab by comms.isAdminOfAnyFlow.collectAsStateWithLifecycle()
    val eventItems by events.items.collectAsStateWithLifecycle()

    EventListScreen(
            events = eventItems,
            showAddFab = showAddFab,
            onEventClick = { position ->
                eventViewModel.withPosition(position)
                onNavigateToEventDetails()
            },
            onAddEventClick = onNavigateToCreateEvent,
    )
}

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
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding),
                    contentAlignment = Alignment.Center,
            ) {
                Text(
                        text = stringResource(id = R.string.events_list_no_events),
                        fontSize = 20.sp,
                )
            }
        } else {
            LazyColumn(
                    contentPadding = innerPadding,
                    modifier = Modifier
                            .fillMaxSize()
                            .consumeWindowInsets(innerPadding),
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
