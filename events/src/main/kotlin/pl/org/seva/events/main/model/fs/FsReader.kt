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

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.*
import pl.org.seva.events.comm.Comm
import pl.org.seva.events.event.Event
import pl.org.seva.events.login.isLoggedIn
import pl.org.seva.events.login.login
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.view.nextColor
import java.time.LocalDateTime
import java.time.ZoneOffset

val fsReader by instance<FsReader>()

class FsReader : FsBase() {

    suspend fun readEvents(community: String) =
            community.events
                    .whereGreaterThan(Event.Fs.TIMESTAMP, earliestEventTime)
                    .read()
                    .map { it.toEvent() }

    suspend infix fun isAdmin(name: String): Boolean = if (isLoggedIn)
            name.admins.document(login.email).doesExist() else false

    suspend fun findCommunity(name: String) = coroutineScope {
        val lcName = name.toLowerCase()
        val deferredComm = async {
            communities.document(lcName).read().toCommunity()
        }
        val isAdmin = async { isAdmin(lcName) }
        val comm = deferredComm.await()
        comm.originalName = name

        if (comm.isDummy) comm else comm.copy(isAdmin = isAdmin.await())
    }

    private suspend fun DocumentReference.doesExist() = read().exists()

    private fun DocumentSnapshot.toEvent(): Event {
        val name: String = getString(EVENT_NAME)!!
        val location: GeoPoint? = getGeoPoint(EVENT_LOCATION)
        val time: LocalDateTime = LocalDateTime.parse(getString(EVENT_TIME))
        val desc: String? = getString(EVENT_DESC)
        return Event(name, time = time, location = location, desc = desc)
    }

    private fun DocumentSnapshot.toCommunity() =
            if (exists()) Comm(
                    name = getString(COMM_NAME)!!,
                    color = nextColor,
                    desc = getString(COMM_DESC) ?: "") else Comm.DUMMY

    companion object {
        val earliestEventTime get() = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    }
}
