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

import androidx.room.PrimaryKey
import pl.org.seva.events.main.data.db.EventsDb
import pl.org.seva.events.main.init.instance
import java.time.LocalDateTime

data class Message(val time: LocalDateTime, val content: String) {

    private val messages by instance<Messages>()

    suspend fun delete() {
        messages delete this
    }

    @androidx.room.Entity(tableName = EventsDb.MESSAGE_TABLE)
    class Entity() {
        @PrimaryKey
        var time: String = ""
        var content: String = ""

        constructor(message: Message) : this() {
            time = message.time.toString()
            content = message.content
        }

        fun value() = Message(
                time = LocalDateTime.parse(time),
                content = content,
        )
    }
}
