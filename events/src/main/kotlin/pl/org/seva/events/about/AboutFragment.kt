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

package pl.org.seva.events.about

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.inBrowser
import pl.org.seva.events.main.extension.versionName
import pl.org.seva.events.main.ui.EventsTheme

private const val VERSION_PLACEHOLDER = "[ver]"

@Composable
fun AboutRoute() {
    val context = LocalContext.current
    val githubUrl = stringResource(id = R.string.about_github_url)
    val btcAddress = stringResource(id = R.string.about_btc_address)
    AboutScreen(
            version = context.versionName,
            onGithubClick = { context.inBrowser(githubUrl) },
            onDonateClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("", btcAddress))
            },
    )
}

@Composable
fun AboutScreen(
        version: String,
        onGithubClick: () -> Unit,
        onDonateClick: () -> Unit,
) {
    Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
                text = stringResource(id = R.string.about_version)
                        .replace(VERSION_PLACEHOLDER, version),
                modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
                text = stringResource(id = R.string.about_developer),
        )
        TextButton(onClick = onGithubClick) {
            Text(text = stringResource(id = R.string.about_github))
        }
        Text(
                text = stringResource(id = R.string.about_donate_btc),
        )
        TextButton(onClick = onDonateClick) {
            Text(text = stringResource(id = R.string.about_btc_address))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    EventsTheme {
        AboutScreen(
                version = "1.0.5",
                onGithubClick = {},
                onDonateClick = {},
        )
    }
}
