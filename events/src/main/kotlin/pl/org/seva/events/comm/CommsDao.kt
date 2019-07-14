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

package pl.org.seva.events.comm

import androidx.room.*
import pl.org.seva.events.main.data.db.EventsDb

@Dao
abstract class CommsDao {

    suspend infix fun delete(comm: Comm) = delete(Comm.Entity(comm))
    open suspend infix fun add(comm: Comm) = insert(Comm.Entity(comm))
    suspend fun getAllValues() = getAll().map { it.value() }
    suspend infix fun add(comms: Collection<Comm>) = insert(comms.map { Comm.Entity(it) })

    @Query("select * from ${EventsDb.COMM_TABLE}")
    abstract suspend fun getAll(): List<Comm.Entity>

    @Insert
    abstract suspend fun insert(comm: Comm.Entity)

    @Insert
    abstract suspend fun insert(comms: Collection<Comm.Entity>)

    @Delete
    abstract suspend fun delete(comm: Comm.Entity)

    @Update
    abstract suspend fun update(comm: Comm.Entity)

    @Query("delete from ${EventsDb.COMM_TABLE}")
    abstract suspend fun clear()
}
