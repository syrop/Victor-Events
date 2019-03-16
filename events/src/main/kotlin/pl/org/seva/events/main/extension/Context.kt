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

package pl.org.seva.events.main.extension

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

fun Context.question(message: String, yes: () -> Unit = {}, no: () -> Unit = {}) {
    val listener = YesNoListener(yes, no)

    AlertDialog.Builder(this@question)
            .setMessage(message)
            .setPositiveButton(android.R.string.yes, listener)
            .setNegativeButton(android.R.string.no, listener).show()
}

private class YesNoListener(
        private val yes: () -> Unit = {},
        private val no: () -> Unit = {}): DialogInterface.OnClickListener {
    override fun onClick(dialog: DialogInterface, which: Int) = when (which) {
        DialogInterface.BUTTON_POSITIVE -> {
            dialog.dismiss()
            yes()
        }
        DialogInterface.BUTTON_NEGATIVE -> {
            dialog.dismiss()
            no()
        }
        else -> Unit
    }
}
