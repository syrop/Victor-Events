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

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.data.LiveRepository
import pl.org.seva.events.main.data.db.db

val messages by instance<Messages>()

class Messages : LiveRepository() {
    private val messageCache = mutableListOf<Message>()
    private val messagesDao by lazy { db.messagesDao }

    val size get() = messageCache.size

    fun isEmpty() = messageCache.isEmpty()

    operator fun get(position: Int) = messageCache[position]

    suspend fun fromDb() {
        messageCache.addAll(db.messagesDao.getAllValues())
        notifyDataSetChanged()
    }

    suspend infix fun addAll(messages: Collection<Message>) = coroutineScope {
        messageCache.addAll(messages)
        launch { messagesDao addAll messages }
        notifyDataSetChanged()
    }

    suspend infix fun delete(message: Message) = coroutineScope {
        messageCache.remove(message)
        launch { messagesDao delete message }
        notifyDataSetChanged()
    }
}
