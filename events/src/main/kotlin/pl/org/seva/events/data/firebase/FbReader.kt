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

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import pl.org.seva.events.data.model.Community
import pl.org.seva.events.data.model.Event

class FbReader : Fb() {

    fun readEvents(community: String): Observable<Event> {
        return community.events.read()
                .concatMapIterable { it.children }
                .filter { it.exists() }
                .map { it.toEvent() }
    }

    private fun String.isAdmin(email: String): Observable<Boolean> = admins.child(email.to64()).doesExist()

    fun findCommunity(name: String, onResult: Community.() -> Unit) {
        val doesExist = communities.child(name).doesExist()
        val isAdmin = if (login.isLoggedIn) name.isAdmin(login.email)
            else Observable.just(false)

        doesExist.zipWith(
                isAdmin,
                BiFunction { exists: Boolean, isAdmin: Boolean ->
                    if (exists) Community("", admin = isAdmin) else Community.empty(name) })
                .subscribe(onResult)
    }

    private fun DatabaseReference.doesExist() = read().map { it.exists() }

    private fun DatabaseReference.read(): Observable<DataSnapshot> {
        val resultSubject = PublishSubject.create<DataSnapshot>()
        return resultSubject
                .doOnSubscribe { addListenerForSingleValueEvent(RxValueEventListener(resultSubject)) }
                .take(READ_ONCE)
    }

    private fun DataSnapshot.toEvent(): Event {
        val name = child(EVENT_NAME).value as String
        val lat = child(EVENT_LAT).value as Double?
        val lon = child(EVENT_LON).value as Double?
        val time = child(EVENT_TIME).value as Long
        val desc = child(EVENT_DESC).value as String?
        return Event(name, time = time, lat = lat, lon = lon, desc = desc)
    }

    companion object {
        val READ_ONCE = 1L
    }
}
