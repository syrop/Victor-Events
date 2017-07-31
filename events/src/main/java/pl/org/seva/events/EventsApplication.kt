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
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.conf.global
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import com.google.firebase.auth.FirebaseUser
import pl.org.seva.events.model.Communities
import pl.org.seva.events.model.Login
import pl.org.seva.events.model.firebase.FbReader
import pl.org.seva.events.model.firebase.FbWriter
import pl.org.seva.events.model.room.EventsDatabase

class EventsApplication: Application(), KodeinGlobalAware {

    private val bootstrap: Bootstrap get() = instance()

    private val eventsModule = Kodein.Module {
        bind<Bootstrap>() with singleton { Bootstrap() }
        bind<FbReader>() with singleton { FbReader() }
        bind<Communities>() with singleton { Communities() }
        bind<Login>() with singleton { Login() }
        bind<FbWriter>() with singleton { FbWriter() }
        bind<EventsDatabase>() with singleton { EventsDatabase() }
    }

    init {
        Kodein.global.addImport(eventsModule)
    }

    override fun onCreate() {
        super.onCreate()
    }

    fun login(user: FirebaseUser) = bootstrap.login(user)
    fun logout() = bootstrap.logout()
}
