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

package pl.org.seva.events.main.fs

import com.google.firebase.auth.FirebaseUser
import pl.org.seva.events.comm.Comm
import pl.org.seva.events.event.Event
import pl.org.seva.events.main.instance

val fsWriter get() = instance<FsWriter>()

class FsWriter : FsBase() {

    fun login(user: FirebaseUser) {}

    fun create(comm: Comm) {
        comm writeEvent Event.CREATION_EVENT
        comm.writeName()
    }

    fun grantAdmin(comm: Comm, email: String) {
        grantAdmin(comm.lowerCaseName, email)
    }

    private fun grantAdmin(community: String, email: String) {
        community.admins.document(email).set(mapOf(FsBase.ADMIN_GRANTED to true))
    }

    private infix fun Comm.writeEvent(event: Event) {
        lowerCaseName.events.document(event.time.toEpochSecond().toString()).set(event.firestore)
    }

    private fun Comm.writeName() {
        lowerCaseName.document.set(mapOf(FsBase.NAME to name))
    }
}
