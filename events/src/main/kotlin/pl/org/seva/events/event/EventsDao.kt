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

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Insert
import pl.org.seva.events.main.data.db.EventsDb

@Dao
abstract class EventsDao {

    open suspend infix fun add(event: Event) = insert(Event.Entity(event))
    suspend infix fun addAll(events: Collection<Event>) = insertAll(events.map { Event.Entity(it) })
    suspend infix fun delete(event: Event) = delete(Event.Entity(event))
    suspend inline fun getAllValues() = getAll().map { it.value() }

    @Query("select * from ${EventsDb.EVENT_TABLE}")
    abstract suspend fun getAll(): List<Event.Entity>

    @Insert
    abstract suspend fun insertAll(events: Collection<Event.Entity>)

    @Insert
    abstract suspend fun insert(event: Event.Entity)

    @Delete
    abstract suspend fun delete(event: Event.Entity)

    @Query("delete from ${EventsDb.EVENT_TABLE}")
    abstract suspend fun clear()
}
