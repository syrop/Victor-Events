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

package pl.org.seva.events.comm

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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.org.seva.events.R
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.EventsTheme

private const val NAME_PLACEHOLDER = "[name]"

@Composable
fun CommListRoute(
        commViewModel: CommViewModel,
        onNavigateToCommDetails: () -> Unit,
        onNavigateToAddComm: () -> Unit,
) {
    val context = LocalContext.current
    val comms by instance<Comms>()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var commItems by remember {
        mutableStateOf((0 until comms.size).map { comms[it] })
    }

    LaunchedEffect(comms) {
        comms.updates.collect {
            commItems = (0 until comms.size).map { comms[it] }
        }
    }

    CommListScreen(
            comms = commItems,
            snackbarHostState = snackbarHostState,
            onCommClick = { position ->
                commViewModel.withPosition(position)
                onNavigateToCommDetails()
            },
            onCommSwipe = { comm ->
                scope.launch {
                    comm.leave()
                    val result = snackbarHostState.showSnackbar(
                            message = context.getString(R.string.comm_list_leave)
                                    .replace(NAME_PLACEHOLDER, comm.name),
                            actionLabel = context.getString(R.string.comm_list_undo),
                            duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        comm.join()
                    }
                }
            },
            onAddCommClick = onNavigateToAddComm,
    )
}

@Composable
fun CommListScreen(
        comms: List<Comm>,
        snackbarHostState: SnackbarHostState,
        onCommClick: (Int) -> Unit,
        onCommSwipe: (Comm) -> Unit,
        onAddCommClick: () -> Unit,
) {
    Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddCommClick) {
                    Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.add_comm_label),
                    )
                }
            },
    ) { innerPadding ->
        if (comms.isEmpty()) {
            Box(
                    modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment = Alignment.Center,
            ) {
                Text(
                        text = stringResource(id = R.string.comm_list_no_comms),
                        fontSize = 20.sp,
                )
            }
        } else {
            LazyColumn(
                    modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
            ) {
                itemsIndexed(
                        items = comms,
                        key = { _, item -> item.name },
                ) { index, comm ->
                    val dismissState = rememberSwipeToDismissBoxState()
                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                            onCommSwipe(comm)
                        }
                    }
                    SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {},
                    ) {
                        CommRow(
                                name = comm.name,
                                color = comm.color,
                                onClick = { onCommClick(index) },
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommListScreenPreview() {
    EventsTheme {
        CommListScreen(
                comms = emptyList(),
                snackbarHostState = remember { SnackbarHostState() },
                onCommClick = {},
                onCommSwipe = {},
                onAddCommClick = {},
        )
    }
}
