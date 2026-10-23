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
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import pl.org.seva.events.main.extension.back
import pl.org.seva.events.main.extension.bold
import pl.org.seva.events.main.extension.boldAnnotated
import pl.org.seva.events.main.extension.longToast
import pl.org.seva.events.main.extension.title
import pl.org.seva.events.main.ui.EventsTheme
import pl.org.seva.events.main.ui.composeContent

class CommDeleteFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ) = composeContent {
        val comm = commViewModel.value.comm
        title = getString(R.string.comm_delete_title).replace(NAME_PLACEHOLDER, comm.name)
        CommDeleteScreen(
                commName = comm.name,
                onCancelClick = { back() },
                onOkClick = {
                    lifecycleScope.launch {
                        comm.delete()
                        longToast(getString(R.string.comm_delete_toast).bold(NAME_PLACEHOLDER, comm.name))
                        back()
                    }
                },
        )
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
    }
}

/**
 * Jetpack Compose screen replacing `res/layout/fr_comm_delete.xml`.
 */
@Composable
fun CommDeleteScreen(
        commName: String,
        onCancelClick: () -> Unit,
        onOkClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
                text = stringResource(id = R.string.comm_delete_prompt)
                        .boldAnnotated(CommDeleteFragment.NAME_PLACEHOLDER, commName),
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
            Button(onClick = onOkClick) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommDeleteScreenPreview() {
    EventsTheme {
        CommDeleteScreen(
                commName = "KotlinUG",
                onCancelClick = {},
                onOkClick = {},
        )
    }
}
