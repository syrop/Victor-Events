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

package pl.org.seva.events.data.firestore

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import pl.org.seva.events.comm.Community
import pl.org.seva.events.event.Event
import pl.org.seva.events.main.instance
import pl.org.seva.events.main.neverDispose
import java.time.ZonedDateTime

fun fsReader() = instance<FsReader>()

class FsReader : FsBase() {

    fun readEvents(community: String): Observable<Event> {
        return community.events.read()
                .filter {it.exists() }
                .map { it.toEvent() }
    }

    private fun String.isAdmin(email: String): Observable<Boolean> = admins.document(email).doesExist()

    fun findCommunity(name: String, onResult: Community.() -> Unit) {
        val lcName = name.toLowerCase()
        val found = communities.document(lcName).read().map { it.toCommunity(name) }

        val isAdminObservable = if (login.isLoggedIn) lcName.isAdmin(login.email)
            else Observable.just(false)

        found.zipWith(
                isAdminObservable,
                BiFunction { comm: Community, isAdmin: Boolean ->
                    if (comm.empty) comm else comm.copy(admin = isAdmin) })
                .subscribe(onResult).neverDispose()

    }

    private fun DocumentReference.doesExist() = read().map { it.exists() }

    private fun DocumentReference.read(): Observable<DocumentSnapshot> {
        val resultSubject = PublishSubject.create<DocumentSnapshot>()
        return resultSubject
                .doOnSubscribe { get().addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        resultSubject.onNext(result.result)
                    } else {
                        resultSubject.onError(result.exception!!)
                    }
                }
                }
    }

    private fun CollectionReference.read(): Observable<DocumentSnapshot> {
        val resultSubject = PublishSubject.create<DocumentSnapshot>()
        return resultSubject.doOnSubscribe { get().addOnCompleteListener { result ->
            if (result.isSuccessful) {
                result.result!!.forEach { element -> resultSubject.onNext(element) }
            } else {
                resultSubject.onError(result.exception!!)
            }
        }
        }
    }

    private fun DocumentSnapshot.toEvent(): Event {
        val name: String = getString(EVENT_NAME)!!
        val location: GeoPoint? = getGeoPoint(EVENT_LOCATION)
        val time: ZonedDateTime = ZonedDateTime.parse(getString(EVENT_TIME))
        val desc: String? = getString(EVENT_DESC)
        return Event(name, time = time, location = location, desc = desc)
    }

    private fun DocumentSnapshot.toCommunity(name: String) =
            if (exists()) Community(getString(NAME)!!) else Community.empty(name)
}
