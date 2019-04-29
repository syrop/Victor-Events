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
import pl.org.seva.events.main.extension.concurrent
import pl.org.seva.events.main.model.fs.fsWriter
import pl.org.seva.events.main.model.fs.fsReader
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.model.LiveRepository
import pl.org.seva.events.main.model.db.db
import pl.org.seva.events.main.model.io
import pl.org.seva.events.main.view.nextColor

val comms by instance<Comms>()

class Comms : LiveRepository() {
    private val commsCache = mutableListOf<Comm>()
    private val commDao by lazy { db.commDao }

    val size get() = commsCache.size

    private val isAdminOf get() = commsCache.filter { it.isAdmin }

    val isAdminOfAny get() = commsCache.any { it.isAdmin }

    val isEmpty get() = commsCache.isEmpty()

    val namesIsAdminOf get() = isAdminOf.map { it.name }.toTypedArray()

    operator fun get(index: Int) = commsCache[index]

    operator fun get(name: String) = commsCache.firstOrNull { it.name == name } ?: Comm.DUMMY

    infix fun contains(comm: Comm) = commsCache.any { it.name == comm.name }

    infix fun update(comm: Comm) = with(comm) {
        commsCache.remove(get(name))
        commsCache.add(this)
        io { commDao update this@with }
        fsWriter update this
        notifyDataSetChanged()
        true
    }

    infix fun leave(comm: Comm) = comm.run {
        commsCache.remove(this).also { updated ->
            if (updated) {
                io { commDao delete this@run }
                notifyDataSetChanged()
            }
        }
    }

    infix fun delete(comm: Comm): Boolean {
        return leave(comm).also { updated ->
            if (updated) fsWriter delete comm
        }
    }

    infix fun join(comm: Comm) = comm.run {
        (!commsCache.contains(comm) && commsCache.add(comm)).also { updated ->
            if (updated) {
                io { commDao add this@run }
                notifyDataSetChanged()
            }
        }
    }

    suspend fun fromDb() {
        commsCache.addAll(commDao.getAllValues())
        notifyDataSetChanged()
    }

    private suspend fun refresh(transform: suspend (Comm) -> Comm): List<Comm> = coroutineScope {
        val commCopy = commsCache.toList()
        val transformed = mutableListOf<Comm>()

        commCopy.concurrent { transformed.add(transform(it)) }
        commsCache.clear()
        commsCache.addAll(transformed.filter { !it.isDummy })
        commDao.clear()
        commsCache.concurrent { commDao add it }
        notifyDataSetChanged()
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
