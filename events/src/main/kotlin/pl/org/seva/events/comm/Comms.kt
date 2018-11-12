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
import pl.org.seva.events.tools.firestore.fsWriter
import pl.org.seva.events.tools.db.db
import pl.org.seva.events.tools.instance
import pl.org.seva.events.tools.ui.colorFactory
import pl.org.seva.events.login.login

val communities get() = instance<Comms>()

fun Comm.join() = communities join this

class Comms {

    private val cache = mutableListOf<Comm>()
    private val login = login()
    private val commDao = db().commDao

    private val size get() = cache.size
    val isEmpty get() = size == 0

    operator fun get(index: Int) = cache[index]

    infix fun join(comm: Comm) {
        cache.add(comm)
        GlobalScope.launch {
            commDao.insert(CommEntity(comm))
        }
    }

    fun addAll(comms: Collection<Comm>) {
        this.cache.addAll(comms)
    }

    val isAdminOf get() = cache.filter { it.admin }

    val isAdminOfAny get() = cache.any { it.admin }

    infix fun joinNewCommunity(name: String) =
        Comm(name, colorFactory().nextColor(), true).apply {
            fsWriter.create(this)
            fsWriter.grantAdmin(this, login.email)
            join()
        }
}
