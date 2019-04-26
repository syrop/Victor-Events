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
import pl.org.seva.events.main.model.db.EventsDb
import pl.org.seva.events.main.model.db.db

val commDao by lazy { db.commDao }

suspend infix fun CommDao.delete(comm: Comm) = delete(Comm.Entity(comm))

suspend infix fun CommDao.join(comm: Comm) = insert(Comm.Entity(comm))

suspend infix fun CommDao.update(comm: Comm) = update(Comm.Entity(comm))

suspend fun CommDao.getAllValues() = getAll().map { it.value() }

@Dao
interface CommDao {

    @Query("select * from ${EventsDb.COMMUNITIES_TABLE_NAME}")
    suspend fun getAll(): List<Comm.Entity>

    @Insert
    suspend fun insert(comm: Comm.Entity)

    @Delete
    suspend fun delete(comm: Comm.Entity)

    @Update
    suspend fun update(comm: Comm.Entity)

    @Query("delete from ${EventsDb.COMMUNITIES_TABLE_NAME}")
    suspend fun clear()
}
