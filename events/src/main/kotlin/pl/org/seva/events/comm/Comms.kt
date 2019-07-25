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

import kotlinx.coroutines.*
import pl.org.seva.events.event.Events
import pl.org.seva.events.main.data.LiveRepository
import pl.org.seva.events.main.data.firestore.FsReader
import pl.org.seva.events.main.data.firestore.FsWriter
import pl.org.seva.events.main.extension.async
import pl.org.seva.events.main.ui.ColorFactory

open class Comms(
        private val fsReader: FsReader,
        private val fsWriter: FsWriter,
        private val commsDao: CommsDao,
        private val events: Events,
        private val colorFactory: ColorFactory) : LiveRepository() {

    private val commsCache = mutableListOf<Comm>()

    val size get() = commsCache.size

    private val isAdminOf get() = commsCache.filter { it.isAdmin }

    val isAdminOfAny get() = commsCache.any { it.isAdmin }

    val isEmpty get() = commsCache.isEmpty()

    val namesIsAdminOf get() = isAdminOf.map { it.name }.toTypedArray()

    operator fun get(index: Int) = commsCache[index]

    operator fun get(name: String) = commsCache.firstOrNull { it.name == name } ?: Comm.DUMMY

    infix fun contains(comm: Comm) = commsCache.any { it.name == comm.name }

    infix fun update(comm: Comm) {
        commsCache.remove(get(comm.name))
        commsCache.add(comm)
        notifyDataSetChanged()
        fsWriter update comm
    }

    suspend infix fun leave(comm: Comm): Boolean = withContext(NonCancellable) {
        val updated = commsCache.remove(comm)
        if (updated) {
            notifyDataSetChanged()
            launch { commsDao delete comm }
            launch { events deleteLocallyFrom comm }
        }
        updated
    }

    suspend infix fun delete(comm: Comm) {
        if (leave(comm)) {
            fsWriter delete comm
        }
    }

    suspend infix fun join(comm: Comm): Boolean = withContext(NonCancellable) {
        val updated = !commsCache.contains(comm) && commsCache.add(comm)
        if (updated) {
            launch { events addFrom comm }
            launch { commsDao add comm }
            notifyDataSetChanged()
        }
        updated
    }

    suspend fun fromDb() {
        commsCache.addAll(commsDao.getAllValues().sortedBy { it.lcName })
        notifyDataSetChanged()
    }

    suspend fun <R> map(block: suspend (Comm) -> R) = commsCache.toList().map { block(it) }

    suspend fun refreshAdminStatuses() =
        refresh { it.copy(isAdmin = fsReader.isAdmin(it.lcName)) }

    suspend fun refresh() =
        refresh { fsReader.findCommunity(it.name).copy(color = it.color) }

    private suspend fun refresh(transform: suspend (Comm) -> Comm): List<Comm> =
            withContext(Dispatchers.Default) {
                val transformed = commsCache
                        .toList()
                        .async { transform(it) }
                        .awaitAll()
                withContext(NonCancellable) {
                    commsCache.clear()
                    commsCache.addAll(transformed.filter { !it.isDummy })
                    commsDao.clear()
                    commsDao add commsCache
                    notifyDataSetChanged()
                }
                transformed
            }

    suspend infix fun joinNewCommunity(name: String) = withContext(NonCancellable) {
        Comm(name, color = colorFactory.nextColor, isAdmin = true).apply {
            fsWriter create this
            fsWriter grantAdmin this
            join()
        }
    }
}
