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

package pl.org.seva.events.main.model.fs

import com.google.firebase.firestore.SetOptions
import pl.org.seva.events.comm.Comm
import pl.org.seva.events.event.Event
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.login.login
import java.time.ZoneOffset

val fsWriter by instance<FsWriter>()

class FsWriter : FsBase() {

    infix fun create(comm: Comm) {
        comm.writeEvent(Event.CREATION_EVENT)
        comm.writeName()
    }

    infix fun update(comm: Comm) = with(comm) {
        lcName.comm.set(mapOf(COMM_NAME to name, COMM_DESC to desc), SetOptions.merge())
    }

    infix fun delete(comm: Comm) {
        comm.lcName.comm.delete()
        comm.lcName.privateComm.delete()
    }

    infix fun grantAdmin(comm: Comm) {
        grantAdmin(comm.lcName, login.email)
    }

    private fun grantAdmin(community: String, email: String) {
        community.admins.document(email).set(mapOf(ADMIN_GRANTED to true))
    }

    private infix fun Comm.writeEvent(event: Event) {
        lcName.events.document(event.time.toEpochSecond(ZoneOffset.UTC).toString()).set(event.fsEvent)
    }

    private fun Comm.writeName() {
        lcName.comm.set(mapOf(COMM_NAME to name))
    }
}
