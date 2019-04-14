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

package pl.org.seva.events.login

import com.google.firebase.auth.FirebaseUser
import pl.org.seva.events.main.init.instance

val login by instance<Login>()

val isLoggedIn get() = login.isLoggedIn

class Login {
    var isLoggedIn: Boolean = false
    var email: String = ""
    var displayName: String = ""

    fun setCurrentUser(user: FirebaseUser?) = if (user != null) {
        isLoggedIn = true
        email = user.email!!
        displayName = user.displayName!!
    } else {
        isLoggedIn = false
        email = ""
        displayName = ""
    }
}
