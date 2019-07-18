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

open class Events(
        private val fsReader: FsReader,
        private val fsWriter: FsWriter,
        private val eventsDao: EventsDao) : LiveRepository() {

    private val eventsCache = mutableListOf<Event>()

    val size get() = eventsCache.size

    suspend fun add(vararg events: Event) = withContext(NonCancellable) {
        eventsCache.addAll(events)
        launch { eventsDao addAll events.toList() }
        fsWriter.addAll(*events)
        notifyDataSetChanged()
    }

    val isEmpty get() = eventsCache.size == 0

    open suspend infix fun addFrom(comm: Comm) {
        val events = fsReader readEventsFrom comm.lcName
        add(*events.toTypedArray())
    }

    suspend infix fun deleteLocallyFrom(comm: Comm) = coroutineScope {
        val events = eventsCache.filter { it.comm == comm.name }
        events.onEach { event ->
            eventsCache.remove(event)
            launch { eventsDao delete event }
        }
        notifyDataSetChanged()
    }

    suspend fun loadFromDb() {
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
