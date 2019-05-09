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

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import pl.org.seva.events.event.events
import pl.org.seva.events.main.extension.launchEach
import pl.org.seva.events.main.model.fs.fsWriter
import pl.org.seva.events.main.model.fs.fsReader
import pl.org.seva.events.main.model.instance
import pl.org.seva.events.main.model.LiveRepository
import pl.org.seva.events.main.model.db.db
import pl.org.seva.events.main.model.io
import pl.org.seva.events.main.view.ui.nextColor

val comms by instance<Comms>()

class Comms : LiveRepository() {
    private val commsCache = mutableListOf<Comm>()
    private val commDao by lazy { db.commsDao }

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

    infix fun leave(comm: Comm) = commsCache.remove(comm).also { updated ->
        if (updated) {
            notifyDataSetChanged()
            io { commDao delete comm }
            io { events deleteFrom comm }
        }
    }

    infix fun delete(comm: Comm) = leave(comm).also { updated ->
        if (updated) fsWriter delete comm
    }

    infix fun join(comm: Comm) =
            (!commsCache.contains(comm) && commsCache.add(comm)).also { updated ->
                if (updated) {
                    notifyDataSetChanged()
                    io { events addFrom comm }
                    io { commDao add comm }
                }
            }

    suspend fun fromDb() {
        commsCache.addAll(commDao.getAllValues().sortedBy { it.lcName })
        notifyDataSetChanged()
    }

    suspend fun <R> map(block: suspend (Comm) -> R) = commsCache.toList().map { block(it) }

    private suspend fun refresh(transform: suspend (Comm) -> Comm): List<Comm> = coroutineScope {
        val commCopy = commsCache.toList()
        val transformed = mutableListOf<Comm>()

        commCopy.launchEach { transformed.add(transform(it)) }
                .joinAll()
        commsCache.clear()
        commsCache.addAll(transformed.filter { !it.isDummy })
        commDao.clear()
        notifyDataSetChanged()
        commsCache.launchEach { commDao add it }
        transformed
    }

    suspend fun refreshAdminStatuses() = coroutineScope {
        refresh { it.copy(isAdmin = fsReader.isAdmin(it.lcName)) }
        Unit
    }

    suspend fun refresh() = coroutineScope {
        refresh { fsReader.findCommunity(it.name).copy(color = it.color) }
    }

    infix fun joinNewCommunity(name: String) =
            Comm(name, color = nextColor, isAdmin = true).apply {
                fsWriter create this
                fsWriter grantAdmin this
                join()
            }
}
