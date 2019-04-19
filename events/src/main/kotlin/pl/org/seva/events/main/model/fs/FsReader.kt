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

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.suspendCancellableCoroutine
import pl.org.seva.events.comm.Comm
import pl.org.seva.events.event.Event
import pl.org.seva.events.login.isLoggedIn
import pl.org.seva.events.login.login
import pl.org.seva.events.main.init.instance
import java.time.LocalDateTime
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

val fsReader by instance<FsReader>()

class FsReader : FsBase() {

    fun readEvents(community: String): Observable<Event> {
        return community.events.read()
                .filter { it.exists() }
                .map { it.toEvent() }
    }

    suspend infix fun isAdmin(name: String): Boolean = if (login.isLoggedIn)
            name.admins.document(login.email).doesExist() else false

    suspend fun findCommunity(name: String): Comm {
        val lcName = name.toLowerCase()
        val comm = communities.document(lcName).read().toCommunity()

        val isAdmin = if (isLoggedIn) isAdmin(lcName) else false

        return if (comm.isDummy) comm else comm.copy(isAdmin = isAdmin)
    }

    private suspend fun DocumentReference.doesExist() = read().exists()

    private suspend fun DocumentReference.read(): DocumentSnapshot = suspendCancellableCoroutine { continuation ->
        get().addOnCompleteListener { result ->
            if (result.isSuccessful) {
                continuation.resume(result.result!!)
            }
            else {
                continuation.resumeWithException(result.exception!!)
            }
        }
    }

    private fun CollectionReference.read(): Observable<DocumentSnapshot> {
        val resultSubject = PublishSubject.create<DocumentSnapshot>()
        return resultSubject.doOnSubscribe {
            get().addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    result.result!!.forEach { element -> resultSubject.onNext(element) }
                }
                else {
                    resultSubject.onError(result.exception!!)
                }
            }
        }
    }

    private fun DocumentSnapshot.toEvent(): Event {
        val name: String = getString(EVENT_NAME)!!
        val location: GeoPoint? = getGeoPoint(EVENT_LOCATION)
        val time: LocalDateTime = LocalDateTime.parse(getString(EVENT_TIME))
        val desc: String? = getString(EVENT_DESC)
        return Event(name, time = time, location = location, desc = desc)
    }

    private fun DocumentSnapshot.toCommunity() =
            if (exists()) Comm(getString(NAME)!!) else Comm.DUMMY
}