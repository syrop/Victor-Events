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

package pl.org.seva.events.data.room

import kotlinx.coroutines.*
import pl.org.seva.events.comm.Community
import pl.org.seva.events.main.globalScopeLaunch

inline infix fun CommDao.getAllAsync(crossinline callback: (Collection<Community>) -> Unit) {
    val collectionJob = GlobalScope.async(
            Dispatchers.Default,
            CoroutineStart.DEFAULT,
            null) { getAll().map { it.comValue() } }
    val collection = ArrayList<Community>(0)
    globalScopeLaunch {
        collection.addAll(collectionJob.await())
        callback(collection)
    }
}
