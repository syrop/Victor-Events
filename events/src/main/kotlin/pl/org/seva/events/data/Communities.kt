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

package pl.org.seva.events.data

import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import pl.org.seva.events.data.firebase.FbWriter
import pl.org.seva.events.data.model.Community
import pl.org.seva.events.data.room.EventsDatabase
import pl.org.seva.events.view.ColorFactory

class Communities : KodeinGlobalAware {

    private val communities = mutableListOf<Community>()
    private val cf: ColorFactory = instance()
    private val fbWriter: FbWriter = instance()
    private val login: Login = instance()
    private val commDao = instance<EventsDatabase>().commDao

    private val size get() = communities.size
    val empty get() = size == 0

    operator fun get(index: Int) = communities[index]

    fun join(community: Community) {
        communities.add(community)
        commDao.insert(community)
    }

    val isAdminOfWhich get() = communities.filter { it.admin }

    val isAdminOfAny get() = communities.any { it.admin }

    infix fun joinNewCommunity(name: String) =
        Community(name = name, color = cf.nextColor(), admin = true).apply {
            fbWriter.create(this)
            fbWriter.grantAdmin(this, login.email)
            join(this)
        }
}
