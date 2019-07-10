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

import kotlinx.coroutines.*
import pl.org.seva.events.comm.Comm
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.main.extension.launchEach
import pl.org.seva.events.main.data.LiveRepository
import pl.org.seva.events.main.data.firestore.FsReader
import pl.org.seva.events.main.data.firestore.FsWriter

class Events(
        private val fsReader: FsReader,
        private val fsWriter: FsWriter,
        private val eventsDao: EventsDao) : LiveRepository() {

    private val eventsCache = mutableListOf<Event>()

    val size get() = eventsCache.size

    suspend infix fun add(event: Event) = withContext(NonCancellable) {
        eventsCache.add(event)
        notifyDataSetChanged()
        launch { eventsDao add event }
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
        eventsCache.addAll(eventsDao.getAllValues().sortedBy { it.time })
        notifyDataSetChanged()
    }

    suspend infix fun fromComms(comms: Comms): List<Event> = coroutineScope {
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

    override fun toString(): String = eventsCache.toString()

    operator fun get(index: Int) = eventsCache[index]
}
