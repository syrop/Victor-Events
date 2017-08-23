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

class Communities : KodeinGlobalAware {

    private val communities = mutableListOf<Community>()

    val size get() = communities.size
    val empty get() = size == 0

    operator fun get(index: Int) = communities[index]

    fun join(community: Community) {
        communities.add(community)
    }

    val admin get() = communities.filter { it.admin }

    val isAdmin get() = communities.any { it.admin }

    fun joinNewCommunity(name: String) {
        val community = Community(name = name, admin = true)
        val writer = instance<FbWriter>()
        writer.create(community)
        writer.grantAdmin(community, instance<Login>().email)
        join(community)
    }
}
