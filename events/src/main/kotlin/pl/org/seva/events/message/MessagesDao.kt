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
import pl.org.seva.events.main.data.db.EventsDb

suspend fun MessagesDao.getAllValues() = getAll().map { it.value() }

suspend infix fun MessagesDao.delete(message: Message) = delete(Message.Entity(message))

suspend infix fun MessagesDao.addAll(messages: Collection<Message>) =
        insert(messages.map { Message.Entity(it) })

@Dao
interface MessagesDao {

    @Query("select * from ${EventsDb.MESSAGE_TABLE}")
    suspend fun getAll(): List<Message.Entity>

    @Insert
    suspend fun insert(messages: Collection<Message.Entity>)

    @Insert
    suspend fun insert(message: Message.Entity)

    @Delete
    suspend fun delete(message: Message.Entity)
}
