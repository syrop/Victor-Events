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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import pl.org.seva.events.main.coroutine.ioLaunch
import pl.org.seva.events.main.fs.fsWriter
import pl.org.seva.events.main.fs.fsReader
import pl.org.seva.events.main.instance
import pl.org.seva.events.main.ui.nextColor

val comms by instance<Comms>()

class Comms {

    private val commCache = mutableListOf<Comm>()

    val size get() = commCache.size

    private val isAdminOf get() = commCache.filter { it.isAdmin }

    val isAdminOfAny get() = commCache.any { it.isAdmin }

    fun isEmpty() = commCache.isEmpty()

    fun isNotEmpty() = commCache.isNotEmpty()

    val names get() = commCache.map { it.name }.toTypedArray()

    val namesIsAdminOf get() = isAdminOf.map { it.name }.toTypedArray()

    operator fun get(index: Int) = commCache[index]

    infix fun contain(comm: Comm) = commCache.any { it.name == comm.name }

    infix fun delete(comm: Comm) = commCache.remove(comm)

    infix fun join(comm: Comm) = !commCache.contains(comm) && commCache.add(comm)

    fun addAll(comms: Collection<Comm>) {
        commCache.addAll(comms)
    }

    fun refreshAdminStatuses(): LiveData<Unit> {
        val commArray = commCache.toTypedArray()
        val commObservable =
                Observable.defer { Observable.fromArray(*commArray) }
        val adminsObservable =
                commObservable.flatMap { fsReader.isAdmin(it.lcName) }
        commCache.clear()
        return MutableLiveData<Unit>().apply {
            commObservable.zipWith(
                    adminsObservable,
                    BiFunction { comm: Comm, isAdmin: Boolean -> comm.copy(isAdmin = isAdmin) }).
                    doOnComplete {
                        ioLaunch {
                            commDao.clear()
                            commCache.forEach { commDao join it }
                        }
                        value = Unit
                    }.subscribe { commCache.add(it) }
        }
    }

    infix fun joinNewCommunity(name: String) =
        Comm(name, color = nextColor, isAdmin = true).apply {
            fsWriter create this
            fsWriter grantAdmin this
            join()
        }
}
