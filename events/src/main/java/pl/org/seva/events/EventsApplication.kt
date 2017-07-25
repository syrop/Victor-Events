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

package pl.org.seva.events

import android.app.Application
import com.google.firebase.auth.FirebaseUser
import pl.org.seva.events.model.Login
import pl.org.seva.events.model.room.EventsDatabase
import javax.inject.Inject

class EventsApplication : Application() {

    @Inject
    lateinit var db: EventsDatabase
    @Inject
    lateinit var login: Login

    lateinit var component: EventsComponent

    override fun onCreate() {
        super.onCreate()
        component = createComponent()
        component.inject(this)
        db.initWithContext(this)
    }

    private fun createComponent(): EventsComponent {
        return DaggerEventsComponent.create()
    }

    fun login(user: FirebaseUser) {
        login.setCurrentUser(user)
    }

    fun logout() {
    }
}
