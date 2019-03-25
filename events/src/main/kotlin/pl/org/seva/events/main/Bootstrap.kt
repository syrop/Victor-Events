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

package pl.org.seva.events.main

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import pl.org.seva.events.comm.comms
import pl.org.seva.events.comm.getAllValues
import pl.org.seva.events.login.login
import pl.org.seva.events.main.coroutine.ioLaunch
import pl.org.seva.events.main.db.db
import pl.org.seva.events.message.getAllValues
import pl.org.seva.events.message.messages

val bootstrap by instance<Bootstrap>()

class Bootstrap {

    fun boot() {
        login.setCurrentUser(FirebaseAuth.getInstance().currentUser)
        ioLaunch { comms.addAll(db.commDao.getAllValues()) }
        ioLaunch { messages.addAll(db.messageDao.getAllValues()) }
    }

    fun login(user: FirebaseUser) {
        login.setCurrentUser(user)
    }

    fun logout() {
    }
}
