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

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import pl.org.seva.events.community.Community
import pl.org.seva.events.event.Event
import pl.org.seva.events.main.instance
import pl.org.seva.events.main.neverDispose

fun fbReader() = instance<FbReader>()

class FbReader : Fb() {

    fun readEvents(community: String): Observable<Event> {
        return community.events.read()
                .filter {it.exists() }
                .map { it.toEvent() }
    }

    private fun String.isAdmin(email: String): Observable<Boolean> = admins.document(email.to64()).doesExist()

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
                .doOnSubscribe { get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        resultSubject.onNext(it.result)
                    } else {
                        resultSubject.onError(it.exception!!)
                    }
                }
                }
    }

    private fun CollectionReference.read(): Observable<DocumentSnapshot> {
        val resultSubject = PublishSubject.create<DocumentSnapshot>()
        return resultSubject.doOnSubscribe { get().addOnCompleteListener {
            if (it.isSuccessful) {
                it.result.forEach { resultSubject.onNext(it) }
            } else {
                resultSubject.onError(it.exception!!)
            }
        }
        }
    }

    private fun DocumentSnapshot.toEvent(): Event {
        val name: String = getString(EVENT_NAME)
        val lat: Double? = getDouble(EVENT_LAT)
        val lon: Double? = getDouble(EVENT_LON)
        val time: Long = getLong(EVENT_TIME)
        val desc: String? = getString(EVENT_DESC)
        return Event(name, time = time, lat = lat, lon = lon, desc = desc)
    }

    private fun DocumentSnapshot.toCommunity(name: String) =
            if (exists()) Community(getString(COMM_NAME)!!) else Community.empty(name)
}
