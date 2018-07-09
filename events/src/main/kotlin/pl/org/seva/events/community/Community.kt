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
 *
 * If you like this program, consider donating bitcoin: 36uxha7sy4mv6c9LdePKjGNmQe8eK16aX6
 */

package pl.org.seva.events.community

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class Community(
        val name: String,
        val color: Int = Color.GRAY,
        val admin: Boolean = false) : Parcelable {

    val lcName: String get() = name.toLowerCase()

    @IgnoredOnParcel
    @Transient
    var empty = false

    companion object {
        fun empty(name: String = "") = Community(name).apply { empty = true }
    }
}
