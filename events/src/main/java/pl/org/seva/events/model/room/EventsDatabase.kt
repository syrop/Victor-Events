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

package pl.org.seva.events.model.room

import android.arch.persistence.room.Room
import android.content.Context

class EventsDatabase {

    lateinit var db: EventsDatabaseAbstract

    fun initWithContext(context: Context) {
        db = Room.databaseBuilder(context, EventsDatabaseAbstract::class.java, DATABASE_NAME).build()
    }

    val eventDao get() = db.eventDao()

    companion object {
        val DATABASE_NAME = "events_database"
        const val EVENTS_TABLE_NAME = "events"
    }
}
