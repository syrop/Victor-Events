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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import pl.org.seva.events.R
import pl.org.seva.events.main.data.firestore.FsWriter
import pl.org.seva.events.main.extension.back
import pl.org.seva.events.main.extension.toast
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.EventsTheme
import pl.org.seva.events.main.ui.composeContent

@Suppress("DEPRECATION")
class AdminAddFragment : Fragment() {

    private val fsWriter by instance<FsWriter>()
    private val vm by commViewModel
    private var address by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ) = composeContent {
        AdminAddScreen(
                address = address,
                onAddressChange = { address = it },
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.admin_add, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        fun confirm(): Boolean {
            fsWriter.grantAdmin(vm.comm, address)
            toast(R.string.admin_add_toast)
            back()
            return true
        }

        return when (item.itemId) {
            R.id.action_ok -> confirm()
            else -> super.onOptionsItemSelected(item)
        }
    }
}

/**
 * Jetpack Compose screen replacing `res/layout/fr_admin_add.xml`.
 */
@Composable
fun AdminAddScreen(
        address: String,
        onAddressChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text(text = stringResource(id = R.string.admin_add_address)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
        )
        Box(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                contentAlignment = Alignment.Center,
        ) {
            Text(
                    text = stringResource(id = R.string.admin_add_prompt),
                    textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminAddScreenPreview() {
    EventsTheme {
        AdminAddScreen(
                address = "admin@example.com",
                onAddressChange = {},
        )
    }
}
