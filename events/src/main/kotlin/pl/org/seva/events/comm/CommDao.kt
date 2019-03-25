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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import pl.org.seva.events.main.coroutine.ioLaunch
import pl.org.seva.events.main.db.EventsDb
import pl.org.seva.events.main.db.db

val commDao by lazy { db.commDao }

infix fun CommDao.delete(comm: Comm) = delete(Comm.Entity(comm))

infix fun CommDao.deleteAsync(comm: Comm) = ioLaunch { delete(comm) }

infix fun CommDao.join(comm: Comm) = insert(Comm.Entity(comm))

infix fun CommDao.joinAsync(comm: Comm) = ioLaunch { join(comm) }

fun CommDao.getAllValues() = getAll().map { it.value() }

@Dao
interface CommDao {

    @Query("select * from ${EventsDb.COMMUNITIES_TABLE_NAME}")
    fun getAll(): List<Comm.Entity>

    @Insert
    fun insertAll(vararg comm: Comm.Entity)

    @Insert
    fun insert(comm: Comm.Entity)

    @Delete
    fun delete(comm: Comm.Entity)

    @Query("delete from ${EventsDb.COMMUNITIES_TABLE_NAME}")
    fun clear()
}
