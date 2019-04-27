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

package pl.org.seva.events.main.model.db

import androidx.room.Room
import android.content.Context
import pl.org.seva.events.main.init.instance

val db by instance<EventsDb>()

class EventsDb(context: Context) {

    private val db=
            Room.databaseBuilder(context, EventsDbAbstract::class.java, DATABASE_NAME).build()

    val eventDao by lazy { db.eventDao() }

    val commDao by lazy { db.commDao() }

    val messageDao by lazy { db.messageDao() }

    companion object {
        const val DATABASE_NAME = "events_database"
        const val DATABASE_VERSION = 1

        const val EVENTS_TABLE_NAME = "events"
        const val COMMS_TABLE_NAME = "communities"
        const val MESSAGES_TABLE_NAME = "messages"
    }
}
