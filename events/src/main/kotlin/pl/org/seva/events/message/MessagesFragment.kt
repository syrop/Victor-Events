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

package pl.org.seva.events.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.plus
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.EventsTheme
import pl.org.seva.events.main.ui.composeContent

class MessagesFragment : Fragment() {

    private val messages by instance<Messages>()
    private var messageItems by mutableStateOf(emptyList<Message>())

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ) = composeContent {
        MessagesScreen(
                messages = messageItems,
                onMessageSwipe = { message ->
                    lifecycleScope.launch { message.delete() }
                },
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshList()
        (messages.updatedLiveData() + this) {
            refreshList()
        }
    }

    private fun refreshList() {
        messageItems = (0 until messages.size).map { messages[it] }
    }
}

/**
 * Jetpack Compose screen replacing `res/layout/fr_messages.xml`.
 */
@Composable
fun MessagesScreen(
        messages: List<Message>,
        onMessageSwipe: (Message) -> Unit,
) {
    if (messages.isEmpty()) {
        Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
        ) {
            Text(
                    text = stringResource(id = R.string.system_messages_no_messages),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                    items = messages,
                    key = { it.time.toString() },
            ) { message ->
                val dismissState = rememberSwipeToDismissBoxState()
                LaunchedEffect(dismissState.currentValue) {
                    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                        onMessageSwipe(message)
                    }
                }
                SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {},
                ) {
                    MessageRow(
                            time = message.time.toString(),
                            content = message.content,
                    )
                }
                HorizontalDivider()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessagesScreenPreview() {
    EventsTheme {
        MessagesScreen(
                messages = emptyList(),
                onMessageSwipe = {},
        )
    }
}
