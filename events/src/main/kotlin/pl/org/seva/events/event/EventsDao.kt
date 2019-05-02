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
import pl.org.seva.events.main.model.db.EventsDb

suspend infix fun EventsDao.add(event: Event) = insert(Event.Entity(event))

suspend infix fun EventsDao.addAll(events: Collection<Event>) = insertAll(events.map { Event.Entity(it) })

suspend infix fun EventsDao.delete(event: Event) = delete(Event.Entity(event))

suspend inline fun EventsDao.getAllValues() = getAll().map { it.value() }

@Dao
interface EventsDao {

    @Query("select * from ${EventsDb.EVENT_TABLE}")
    suspend fun getAll(): List<Event.Entity>

    @Insert
    suspend fun insertAll(events: Collection<Event.Entity>)

    @Insert
    suspend fun insert(event: Event.Entity)

    @Delete
    suspend fun delete(event: Event.Entity)

    @Query("delete from ${EventsDb.EVENT_TABLE}")
    suspend fun clear()
}
