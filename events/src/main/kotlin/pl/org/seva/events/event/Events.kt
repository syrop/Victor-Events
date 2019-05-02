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

package pl.org.seva.events.event

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import pl.org.seva.events.comm.Comm
import pl.org.seva.events.comm.comms
import pl.org.seva.events.main.extension.launchEach
import pl.org.seva.events.main.model.instance
import pl.org.seva.events.main.model.LiveRepository
import pl.org.seva.events.main.model.db.db
import pl.org.seva.events.main.model.fs.fsReader
import pl.org.seva.events.main.model.fs.fsWriter
import pl.org.seva.events.main.model.io

val events by instance<Events>()

class Events : LiveRepository() {

    private val eventsCache = mutableListOf<Event>()
    private val eventsDao by lazy { db.eventsDao }

    val size get() = eventsCache.size

    infix fun add(event: Event) {
        eventsCache.add(event)
        notifyDataSetChanged()
        io { eventsDao add event }
        fsWriter add event
    }

    val isEmpty get() = eventsCache.size == 0

    suspend infix fun addFrom(comm: Comm) {
        (fsReader readEventsFrom comm.lcName).also { events ->
            eventsCache.addAll(events)
            notifyDataSetChanged()
            eventsDao addAll events
        }
    }

    suspend infix fun deleteFrom(comm: Comm) {
        eventsCache.filter { it.comm == comm.name }
                .onEach { eventsCache.remove(it) }
                .launchEach { eventsDao delete it }
        notifyDataSetChanged()
    }

    suspend fun fromDb() {
        eventsCache.addAll(eventsDao.getAllValues())
        notifyDataSetChanged()
    }

    suspend fun refresh(): List<Event> = coroutineScope {
        mutableListOf<Event>().apply {
            comms.map {
                async { fsReader readEventsFrom it.lcName }
            }.map {
                it.await()
            }.onEach {
                addAll(it)
            }
        }.apply {
            eventsCache.clear()
            eventsCache.addAll(this)
            notifyDataSetChanged()
            eventsDao.clear()
            launchEach { eventsDao.add(it) }
        }
    }

    operator fun get(index: Int) = eventsCache[index]
}
