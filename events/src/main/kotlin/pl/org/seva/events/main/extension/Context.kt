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

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes

val Context.versionName: String
    get() = checkNotNull(packageManager.getPackageInfo(packageName, 0).versionName)

fun Context.inBrowser(uri: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    startActivity(intent)
}

fun Context.prefs(name: String): SharedPreferences =
        getSharedPreferences(name, Context.MODE_PRIVATE)

fun Context.longToast(message: CharSequence) =
        toast(message, Toast.LENGTH_LONG)

fun Context.toast(@StringRes id: Int, duration: Int = Toast.LENGTH_SHORT) =
        toast(getString(id), duration)

fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    if (message.isNotBlank()) {
        Toast.makeText(this, message, duration).show()
    }
}
