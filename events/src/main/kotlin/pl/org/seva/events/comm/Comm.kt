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
 * If you like this program, consider donating bitcoin: bc1qncxh5xs6erq6w4qz3a7xl7f50agrgn3w58dsfp
 */

package pl.org.seva.events.comm

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import pl.org.seva.events.main.db.EventsDb

@SuppressLint("ParcelCreator")
@Parcelize
data class Comm(
        val name: String,
        val color: Int = Color.GRAY,
        val admin: Boolean = false) : Parcelable {

    val lcName: String get() = name.toLowerCase()

    @IgnoredOnParcel
    @Transient
    var empty = false

    @androidx.room.Entity(tableName = EventsDb.COMMUNITIES_TABLE_NAME)
    class Entity() {
        @PrimaryKey
        lateinit var name: String
        var color: Int = Color.GRAY
        var admin: Boolean = false

        constructor(comm: Comm): this() {
            name = comm.name
            color = comm.color
            admin = comm.admin
        }

        fun comValue() = Comm(name = name, color = color, admin = admin)
    }

    companion object {
        fun empty(name: String = "") = Comm(name).apply { empty = true }
    }
}
