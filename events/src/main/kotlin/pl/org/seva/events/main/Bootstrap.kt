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

package pl.org.seva.events.main

import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import pl.org.seva.events.data.Communities
import pl.org.seva.events.data.Login
import pl.org.seva.events.data.room.EventsDatabase
import pl.org.seva.events.data.room.getAllAsync

class Bootstrap : KodeinGlobalAware {
    private val db: EventsDatabase = instance()
    private val login: Login = instance()
    private val communities: Communities = instance()

    fun boot() {
        login.setCurrentUser(FirebaseAuth.getInstance().currentUser)
        db.commDao getAllAsync { communities.addAll(it) }
    }

    fun login(user: FirebaseUser) {
        login.setCurrentUser(user)
    }

    fun logout() {
    }
}
