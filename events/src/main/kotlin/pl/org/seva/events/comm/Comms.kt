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

import pl.org.seva.events.main.model.fs.fsWriter
import pl.org.seva.events.main.model.fs.fsReader
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.model.LiveRepository
import pl.org.seva.events.main.model.io
import pl.org.seva.events.main.view.nextColor

val comms by instance<Comms>()

class Comms : LiveRepository() {

    private val commCache = mutableListOf<Comm>()

    val size get() = commCache.size

    private val isAdminOf get() = commCache.filter { it.isAdmin }

    val isAdminOfAny get() = commCache.any { it.isAdmin }

    val isEmpty get() = commCache.isEmpty()

    fun isNotEmpty() = commCache.isNotEmpty()

    val names get() = commCache.map { it.name }.toTypedArray()

    val namesIsAdminOf get() = isAdminOf.map { it.name }.toTypedArray()

    operator fun get(index: Int) = commCache[index]

    operator fun get(name: String) = commCache.first { it.name == name }

    infix fun contains(comm: Comm) = commCache.any { it.name == comm.name }

    infix fun update(comm: Comm) {
        delete(get(comm.name))
        join(comm)
    }

    infix fun delete(comm: Comm) = commCache.remove(comm)
            .also { if (it) notifyDataSetChanged() }

    infix fun join(comm: Comm) = (!commCache.contains(comm) && commCache.add(comm))
            .also { if (it) notifyDataSetChanged() }

    fun addAll(comms: Collection<Comm>) {
        commCache.addAll(comms)
        notifyDataSetChanged()
    }

    fun refreshAdminStatuses() = io {
        val commArray = commCache.toTypedArray()
        commCache.clear()
        commArray.forEach { comm ->
            val isAdmin = fsReader.isAdmin(comm.lcName)
            commCache.add(comm.copy(isAdmin = isAdmin))
        }
        commDao.clear()
        commCache.forEach { commDao join it }
        notifyDataSetChanged()
    }

    infix fun joinNewCommunity(name: String) =
        Comm(name, color = nextColor, isAdmin = true).apply {
            fsWriter create this
            fsWriter grantAdmin this
            join()
        }
}
