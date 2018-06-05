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
 * If you like this program, consider donating bitcoin: 37vHXbpPcDBwcCTpZfjGL63JRwn6FPiXTS
 */

package pl.org.seva.events.data.room.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.firebase.firestore.GeoPoint
import pl.org.seva.events.event.Event
import pl.org.seva.events.data.room.EventsDatabase
import java.time.ZonedDateTime

@Entity(tableName = EventsDatabase.EVENTS_TABLE_NAME)
class EventEntity() {
        lateinit var name: String
        @PrimaryKey
        var time: String = ""
        var lat: Double? = null
        var lon: Double? = null
        var desc: String? = null

    fun value() = Event(
            name = name,
            time = ZonedDateTime.parse(time),
            location = lat?.let { GeoPoint(it, lon!!) },
            desc = desc)

    constructor(event: Event): this() {
        name = event.name
        time = event.time.toString()
        lat = event.location?.latitude
        lon = event.location?.longitude
        desc = event.desc
    }
}
