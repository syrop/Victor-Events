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

package pl.org.seva.events.data.room.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import pl.org.seva.events.data.model.Event
import pl.org.seva.events.data.room.EventsDatabase

@Entity(tableName = EventsDatabase.EVENTS_TABLE_NAME)
class EventEntity() {
        lateinit var name: String
        @PrimaryKey
        var time: Long = 0
        var lat: Double? = null
        var lon: Double? = null
        var desc: String? = null

    fun eventValue() = Event(name = name, time = time, lat = lat, desc = desc)

    constructor(event: Event): this() {
        name = event.name
        time = event.time
        lat = event.lat
        lon = event.lon
        desc = event.desc
    }
}
