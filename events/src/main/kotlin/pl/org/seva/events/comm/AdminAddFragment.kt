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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.org.seva.events.R
import pl.org.seva.events.main.ui.EventsTheme

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
