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
import pl.org.seva.events.data.model.Community
import pl.org.seva.events.data.model.Event

class FbWriter : Fb() {

    fun login(user: FirebaseUser) {}

    fun create(community: Community) {
        writeEvent(community, Event.creation)
    }

    fun grantAdmin(community: Community, email: String) {
        grantAdmin(community.name, email)
    }

    fun grantAdmin(community: String, email: String) {
        community.admins child(email.to64()) value DEFAULT_VALUE
    }

    fun writeEvent(community: Community, event: Event) {
        val ref =  community.name.events child event.time.toString()
        ref.child(EVENT_NAME).setValue(event.name)
        event.lat?.apply { ref.child(EVENT_LAT).setValue(this) }
        event.lon?.apply { ref.child(EVENT_LON).setValue(this) }
        event.desc?.apply { ref.child(EVENT_DESC).setValue(this) }
    }

    companion object {
        val DEFAULT_VALUE = 0
    }
}
