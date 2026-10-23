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

package pl.org.seva.events.main.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.fragment.app.Fragment
import pl.org.seva.events.R

/**
 * Material 3 Compose theme mapped from the XML `AppTheme` in `res/values/styles.xml`
 * and color resources in `res/values/colors.xml`.
 */
@Composable
fun EventsTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
            primary = colorResource(id = R.color.colorPrimary),
            primaryContainer = colorResource(id = R.color.colorPrimaryDark),
            secondary = colorResource(id = R.color.colorAccent),
            onSecondary = colorResource(id = R.color.colorOnFab),
            onSurface = colorResource(id = R.color.snackbar),
    )
    MaterialTheme(
            colorScheme = colorScheme,
            content = content,
    )
}

/**
 * Creates a [ComposeView] configured with [ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed]
 * and wrapped in [EventsTheme] for hosting Compose UI inside a [Fragment].
 */
fun Fragment.composeContent(content: @Composable () -> Unit): ComposeView =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                EventsTheme {
                    content()
                }
            }
        }
