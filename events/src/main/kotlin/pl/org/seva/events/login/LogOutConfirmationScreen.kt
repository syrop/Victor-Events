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

package pl.org.seva.events.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.org.seva.events.R
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.EventsTheme

@Composable
fun LogOutConfirmationRoute(
        onBack: () -> Unit,
) {
    val context = LocalContext.current
    val comms by instance<Comms>()
    val login by instance<Login>()
    val scope = rememberCoroutineScope()
    var inProgress by remember { mutableStateOf(false) }

    LogOutConfirmationScreen(
            inProgress = inProgress,
            onCancelClick = onBack,
            onLogOutClick = {
                scope.launch {
                    inProgress = true
                    login.signOut(context)
                    comms.refreshAdminStatuses()
                    onBack()
                }
            },
    )
}

@Composable
fun LogOutConfirmationScreen(
        inProgress: Boolean,
        onCancelClick: () -> Unit,
        onLogOutClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (inProgress) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Text(
                    text = stringResource(id = R.string.log_out_confirmation_prompt),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
            )
            Row(
                    modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TextButton(onClick = onCancelClick) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
                Button(onClick = onLogOutClick) {
                    Text(text = stringResource(id = R.string.log_out_confirmation_button))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogOutConfirmationScreenPreview() {
    EventsTheme {
        LogOutConfirmationScreen(
                inProgress = false,
                onCancelClick = {},
                onLogOutClick = {},
        )
    }
}
