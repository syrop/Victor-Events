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

@file:Suppress("unused")

package pl.org.seva.events.main.data.db

import androidx.room.RoomDatabase
import androidx.room.Database
import pl.org.seva.events.comm.Comm
import pl.org.seva.events.comm.CommsDao
import pl.org.seva.events.event.Event
import pl.org.seva.events.event.EventsDao
import pl.org.seva.events.message.Message
import pl.org.seva.events.message.MessagesDao

@Database(
        entities = [Event.Entity::class, Comm.Entity::class, Message.Entity::class],
        version = EventsDb.DATABASE_VERSION,
        autoMigrations = [
        ],
)
abstract class EventsDbAbstract : RoomDatabase() {
    abstract fun eventsDao(): EventsDao
    abstract fun commsDao(): CommsDao
    abstract fun messagesDao(): MessagesDao
}
