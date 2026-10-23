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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.back
import pl.org.seva.events.main.extension.bold
import pl.org.seva.events.main.extension.nav
import pl.org.seva.events.main.extension.onBack
import pl.org.seva.events.main.extension.toast
import pl.org.seva.events.main.ui.EventsTheme
import pl.org.seva.events.main.ui.composeContent

@Suppress("DEPRECATION")
class CommEditFragment : Fragment() {

    private val vm by commViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        onBack { onBackOrHomePressed() }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ) = composeContent {
        val name by vm.name.observeAsState(vm.comm.name)
        val desc by vm.desc.observeAsState(vm.comm.desc)

        LaunchedEffect(name) {
            if (name == Comm.DUMMY_NAME) {
                back()
            }
        }

        CommEditScreen(
                name = name ?: "",
                desc = desc ?: "",
                onDescChange = { vm.desc.value = it },
                onDeleteClick = { nav(R.id.action_commEditFragment_to_commDeleteFragment) },
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        fun updateAndFinish(): Boolean {
            with(vm.comm.copy(desc = vm.desc.value ?: "")) {
                update()
                vm.update()
                toast(
                        getString(R.string.comm_edit_updated)
                                .bold(NAME_PLACEHOLDER, name),
                )
                back()
            }
            return true
        }

        return when (item.itemId) {
            R.id.action_ok -> updateAndFinish()
            android.R.id.home -> onBackOrHomePressed()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onBackOrHomePressed(): Boolean {
        vm.reset()
        back()
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.comm_edit, menu)
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
    }
}

/**
 * Jetpack Compose screen replacing `res/layout/fr_comm_edit.xml`.
 */
@Composable
fun CommEditScreen(
        name: String,
        desc: String,
        onDescChange: (String) -> Unit,
        onDeleteClick: () -> Unit,
) {
    Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = onDeleteClick) {
                    Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.comm_delete_label),
                    )
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
                    label = { Text(text = stringResource(id = R.string.comm_edit_name)) },
                    modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                    value = desc,
                    onValueChange = onDescChange,
                    label = { Text(text = stringResource(id = R.string.comm_edit_description)) },
                    modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommEditScreenPreview() {
    EventsTheme {
        CommEditScreen(
                name = "KotlinUG",
                desc = "Kotlin User Group",
                onDescChange = {},
                onDeleteClick = {},
        )
    }
}
