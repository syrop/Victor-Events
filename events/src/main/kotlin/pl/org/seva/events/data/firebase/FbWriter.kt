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

package pl.org.seva.events.data.firebase

import com.google.firebase.auth.FirebaseUser
import pl.org.seva.events.community.Community
import pl.org.seva.events.event.Event
import pl.org.seva.events.main.instance

fun fbWriter() = instance<FbWriter>()

class FbWriter : Fb() {

    fun login(user: FirebaseUser) {}

    fun create(community: Community) {
        community writeEvent Event.CREATION_EVENT
        community.writeName()
    }

    fun grantAdmin(community: Community, email: String) {
        grantAdmin(community.lcName, email)
    }

    private fun grantAdmin(community: String, email: String) {
        (community.admins document email).set(DEFAULT_VALUE)
    }

    private infix fun Community.writeEvent(event: Event) {
        val ref =  lcName.events document event.time.toString()
        ref.set(event)
    }

    private fun Community.writeName() {
        lcName.name.document().set(name)
    }

    companion object {
        const val DEFAULT_VALUE = 0
    }
}
