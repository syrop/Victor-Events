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

package pl.org.seva.events.data.room

import android.arch.persistence.room.Room
import android.content.Context
import pl.org.seva.events.main.instance

fun db() = instance<EventsDatabase>()

class EventsDatabase(context: Context) {

    private val db: EventsDatabaseAbstract

    init {
        db = Room.databaseBuilder(context, EventsDatabaseAbstract::class.java, DATABASE_NAME).build()
    }

    val eventDao get() = db.eventDao()

    val commDao get() = db.commDao()

    companion object {
        const val DATABASE_NAME = "events_database"
        const val DATABASE_VERSION = 1

        const val EVENTS_TABLE_NAME = "events"
        const val COMMUNITIES_TABLE_NAME = "communities"
    }
}
