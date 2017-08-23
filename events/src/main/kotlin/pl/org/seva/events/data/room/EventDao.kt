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

package pl.org.seva.events.data.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import pl.org.seva.events.data.model.Event
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert


@Dao
interface EventDao {

    @Query("select * from ${EventsDatabase.EVENTS_TABLE_NAME}")
    fun getAll(): List<Event>

    @Insert
    fun insertAll(vararg events: Event)

    @Delete
    fun delete(event: Event)
}
