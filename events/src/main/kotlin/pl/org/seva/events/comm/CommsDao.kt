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

suspend infix fun CommsDao.delete(comm: Comm) = delete(Comm.Entity(comm))

suspend infix fun CommsDao.add(comm: Comm) = insert(Comm.Entity(comm))

suspend fun CommsDao.getAllValues() = getAll().map { it.value() }

@Dao
interface CommsDao {

    @Query("select * from ${EventsDb.COMM_TABLE}")
    suspend fun getAll(): List<Comm.Entity>

    @Insert
    suspend fun insert(comm: Comm.Entity)

    @Delete
    suspend fun delete(comm: Comm.Entity)

    @Update
    suspend fun update(comm: Comm.Entity)

    @Query("delete from ${EventsDb.COMM_TABLE}")
    suspend fun clear()
}
