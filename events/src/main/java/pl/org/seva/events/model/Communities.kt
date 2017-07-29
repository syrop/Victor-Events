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

package pl.org.seva.events.model

import pl.org.seva.events.model.firebase.FbWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Communities @Inject constructor() {

    @Inject
    lateinit var writer: FbWriter
    @Inject
    lateinit var login: Login

    private val communities = mutableListOf<Community>()
    val size get() = communities.size
    val empty get() = size == 0

    operator fun get(index: Int) = communities[index]

    fun join(community: Community) {
        communities.add(community)
    }

    fun joinNewCommunity(name: String) {
        val community = Community(name, true)
        writer.create(community)
        writer.grantAdmin(community, login.email)
        join(community)
    }
}
