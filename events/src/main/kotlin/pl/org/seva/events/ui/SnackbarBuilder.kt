/*
 * Copyright (C) 2017 Wiktor Nizio
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
 */

package pl.org.seva.events.ui

import android.support.design.widget.Snackbar
import android.view.View

fun longSnackbar(f: SnackbarBuilder.() -> Unit): SnackbarBuilder = SnackbarBuilder().apply(f)

fun permanentSnackbar(f: SnackbarBuilder.() -> Unit): SnackbarBuilder =
        SnackbarBuilder(Snackbar.LENGTH_INDEFINITE).apply(f)

class SnackbarBuilder(private var length: Int = Snackbar.LENGTH_LONG) {
    lateinit var view: View
    var message = 0
    var action = 0

    infix fun show(f: () -> Unit) {
        Snackbar.make(view, message, length).setAction(action, { f() }).show()
    }
}
