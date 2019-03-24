/*
 * Copyright (C) 2019 Wiktor Nizio
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

package pl.org.seva.events.message

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import pl.org.seva.events.main.coroutine.ioLaunch
import pl.org.seva.events.main.db.EventsDb

fun MessageDao.getAllValues() = getAll().map { it.value() }

inline infix fun MessageDao.getAllAsync(crossinline callback: (Collection<Message>) -> Unit) {
    ioLaunch { callback(getAllValues()) }
}

infix fun MessageDao.delete(message: Message) = delete(Message.Entity(message))

infix fun MessageDao.deleteAsync(message: Message) = ioLaunch { delete(message) }

@Dao
interface MessageDao {

    @Query("select * from ${EventsDb.MESSAGES_TABLE_NAME}")
    fun getAll(): List<Message.Entity>

    @Insert
    fun insert(message: Message.Entity)

    @Delete
    fun delete(message: Message.Entity)
}
