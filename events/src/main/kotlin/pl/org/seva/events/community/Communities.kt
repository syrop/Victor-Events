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

package pl.org.seva.events.community

import kotlinx.coroutines.CommonPool
import kotlinx.coroutines.launch
import pl.org.seva.events.data.firestore.fsWriter
import pl.org.seva.events.data.room.db
import pl.org.seva.events.data.room.entity.CommEntity
import pl.org.seva.events.main.instance
import pl.org.seva.events.main.ui.colorFactory
import pl.org.seva.events.login.login

fun communities() = instance<Communities>()

class Communities {

    private val cache = mutableListOf<Community>()
    private val fbWriter = fsWriter()
    private val login = login()
    private val commDao = db().commDao

    private val size get() = cache.size
    val isEmpty get() = size == 0

    operator fun get(index: Int) = cache[index]

    infix fun join(community: Community) {
        cache.add(community)
        launch(CommonPool) {
            commDao.insert(CommEntity(community))
        }
    }

    fun addAll(communities: Collection<Community>) {
        this.cache.addAll(communities)
    }

    val isAdminOf get() = cache.filter { it.admin }

    val isAdminOfAny get() = cache.any { it.admin }

    infix fun joinNewCommunity(name: String) =
        Community(name, colorFactory().nextColor(), true).apply {
            fbWriter.create(this)
            fbWriter.grantAdmin(this, login.email)
            join(this)
        }
}
