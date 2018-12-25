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

import android.app.Application
import com.google.firebase.auth.FirebaseUser
import org.kodein.di.Kodein
import org.kodein.di.conf.global

class EventsApplication : Application() {

    init {
        Kodein.global.addImport(module)
    }

    override fun onCreate() {
        super.onCreate()
        bootstrap.boot()
    }

    fun login(user: FirebaseUser) = bootstrap.login(user)

    fun logout() = bootstrap.logout()
}
