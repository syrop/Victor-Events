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

package pl.org.seva.events.model.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import pl.org.seva.events.model.Event
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FbReader @Inject constructor() : Fb() {

    fun readEvents(community: String): Observable<Event> {
        return community.events.read()
                .concatMapIterable { it.children }
                .filter { it.exists() }
                .map { it.toEvent() }
    }

    fun isAdmin(community: String, email: String): Observable<Boolean> =
        community.admins.child(email.to64()).read().map { it.exists() }

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
        return Event(name, lat, lon, time, desc)
    }

    companion object {
        val READ_ONCE = 1L
    }
}
