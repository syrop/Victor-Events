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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.org.seva.events.R
import pl.org.seva.events.main.ui.EventsTheme

@Composable
fun CommDetailsRoute(
        commViewModel: CommViewModel,
        onNavigateToEdit: () -> Unit,
        onBack: () -> Unit,
) {
    val name by commViewModel.name.collectAsStateWithLifecycle()
    val desc by commViewModel.desc.collectAsStateWithLifecycle()
    val isAdmin by commViewModel.isAdmin.collectAsStateWithLifecycle()

    LaunchedEffect(name) {
        if (name == Comm.DUMMY_NAME) {
            onBack()
        }
    }

    CommDetailsScreen(
            name = name.orEmpty(),
            desc = desc.orEmpty(),
            isAdmin = isAdmin == true,
            onEditClick = onNavigateToEdit,
    )
}

@Composable
fun CommDetailsScreen(
        name: String,
        desc: String,
        isAdmin: Boolean,
        onEditClick: () -> Unit,
) {
    Scaffold(
            floatingActionButton = {
                if (isAdmin) {
                    FloatingActionButton(onClick = onEditClick) {
                        Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(id = R.string.comm_edit_label),
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
            TextField(
                    value = name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(id = R.string.comm_details_name)) },
                    modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                    value = desc,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(id = R.string.comm_details_description)) },
                    modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommDetailsScreenPreview() {
    EventsTheme {
        CommDetailsScreen(
                name = "KotlinUG",
                desc = "Kotlin User Group",
                isAdmin = true,
                onEditClick = {},
        )
    }
}
