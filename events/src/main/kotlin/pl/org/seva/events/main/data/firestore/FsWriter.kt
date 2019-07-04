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

package pl.org.seva.events.main.data.firestore

import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import pl.org.seva.events.comm.Comm
import pl.org.seva.events.event.Event
import pl.org.seva.events.login.Login
import pl.org.seva.events.main.init.instance
import java.util.*

class FsWriter : FsBase() {

    private val login by instance<Login>()

    infix fun create(comm: Comm) {
        comm.writeEvent(Event.creationEvent.copy(comm = comm.name))
        comm.writeName()
    }

    infix fun add(event: Event) = event.comm.toLowerCase(Locale.getDefault()).writeEvent(event)

    infix fun update(comm: Comm) = with(comm) {
        lcName.comm.set(mapOf(COMM_NAME to name, COMM_DESC to desc), SetOptions.merge())
    }

    suspend infix fun delete(comm: Comm) = coroutineScope {
        launch {
            comm.lcName.events.read().map { event ->
                launch { event.reference.delete() }
            }.joinAll()
            comm.lcName.comm.delete()
        }
        launch {
            comm.lcName.admins.read().map { admin ->
                launch {
                    admin.reference.delete()
                }
            }.joinAll()
            comm.lcName.privateComm.delete()
        }
    }

    infix fun grantAdmin(comm: Comm) = grantAdmin(comm, login.email)

    fun grantAdmin(comm: Comm, email: String) =
        comm.lcName.admins.document(email).set(mapOf(ADMIN_GRANTED to true))

    private infix fun Comm.writeEvent(event: Event) = lcName writeEvent  event

    private infix fun String.writeEvent(event: Event) = with (event.fsEvent) {
        events.document(timestamp.toString()).set(this)
    }

    private fun Comm.writeName() = lcName.comm.set(mapOf(COMM_NAME to name))
}
