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
 */

package pl.org.seva.events.community

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import pl.org.seva.events.data.firebase.fbWriter
import pl.org.seva.events.data.room.db
import pl.org.seva.events.data.room.entity.CommEntity
import pl.org.seva.events.main.instance
import pl.org.seva.events.main.ui.colorFactory

fun communities() = instance<Communities>()

class Communities {

    private val communities = mutableListOf<Community>()
    private val cf = colorFactory()
    private val fbWriter = fbWriter()
    private val login = pl.org.seva.events.login.login()
    private val commDao = db().commDao

    private val size get() = communities.size
    val empty get() = size == 0

    operator fun get(index: Int) = communities[index]

    infix fun join(community: Community) {
        communities.add(community)
        launch(CommonPool) {
            commDao.insert(CommEntity(community))
        }
    }

    fun addAll(communities: Collection<Community>) {
        this.communities.addAll(communities)
    }

    val isAdminOf get() = communities.filter { it.admin }

    val isAdminOfAny get() = communities.any { it.admin }

    infix fun joinNewCommunity(name: String) =
        Community(name = name, color = cf.nextColor(), admin = true).apply {
            fbWriter.create(this)
            fbWriter.grantAdmin(this, login.email)
            join(this)
        }
}
