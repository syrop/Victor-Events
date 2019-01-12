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

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.org.seva.events.main.fs.fsWriter
import pl.org.seva.events.main.db.db
import pl.org.seva.events.main.instance
import pl.org.seva.events.main.ui.colorFactory
import pl.org.seva.events.login.login

val comms get() = instance<Comms>()

fun Comm.join() = comms join this

fun String.joinNewCommunity() = comms joinNewCommunity this

class Comms {

    private val cache = mutableListOf<Comm>()
    private val commDao = db.commDao
    private val size get() = cache.size
    val isAdminOf get() = cache.filter { it.admin }

    val isAdminOfAny get() = cache.any { it.admin }

    val isEmpty get() = size == 0

    val namesIsAdminOf get() = isAdminOf.map { it.name }.toTypedArray()

    operator fun get(index: Int) = cache[index]

    infix fun join(comm: Comm) {
        cache.add(comm)
        GlobalScope.launch {
            commDao.insert(Comm.Entity(comm))
        }
    }

    fun addAll(comms: Collection<Comm>) {
        this.cache.addAll(comms)
    }

    infix fun joinNewCommunity(name: String) =
        Comm(name, colorFactory().nextColor(), true).apply {
            fsWriter.create(this)
            fsWriter.grantAdmin(this, login.email)
            join()
        }
}
