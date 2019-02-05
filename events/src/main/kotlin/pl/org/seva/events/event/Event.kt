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

package pl.org.seva.events.event

import android.annotation.SuppressLint
import androidx.room.PrimaryKey
import com.google.firebase.firestore.GeoPoint
import pl.org.seva.events.main.db.EventsDb
import java.time.LocalDateTime

data class Event(
        val name: String = CREATION_NAME,
        val time: LocalDateTime = LocalDateTime.now(),
        val location: GeoPoint? = null,
        val address: String? = null,
        val desc: String? = null
) {

    val fsEvent get() = Fs(
            name = name,
            time = time.toString(),
            location = location,
            address = address,
            desc = desc)

    @Suppress("MemberVisibilityCanBePrivate")
    data class Fs(
            val name: String,
            val time: String,
            val location: GeoPoint?,
            val address: String?,
            val desc: String?) {
        fun value() = Event(
                name = name,
                time = LocalDateTime.parse(time),
                location = location,
                address = address,
                desc = desc)
    }

    @androidx.room.Entity(tableName = EventsDb.EVENTS_TABLE_NAME)
    class Entity {
        lateinit var name: String
        @PrimaryKey
        var time: String = ""
        var lat: Double? = null
        var lon: Double? = null
        var address: String? = null
        var desc: String? = null

        fun value() = Event(
                name = name,
                time = LocalDateTime.parse(time),
                location = lat?.let { GeoPoint(it, lon!!) },
                address = address,
                desc = desc)
    }

    companion object {
        private const val CREATION_NAME = ""
        val CREATION_EVENT = Event(CREATION_NAME)
    }
}
