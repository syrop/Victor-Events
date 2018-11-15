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

package pl.org.seva.events.tools

import android.content.Context
import org.kodein.di.Kodein
import org.kodein.di.conf.global
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.login.Login
import pl.org.seva.events.tools.firestore.FsReader
import pl.org.seva.events.tools.firestore.FsWriter
import pl.org.seva.events.tools.db.EventsDb
import pl.org.seva.events.tools.ui.ColorFactory
import pl.org.seva.events.tools.ui.Toaster

fun Context.module() = KodeinModuleBuilder(this).build()

inline fun <reified R : Any> instance(): R {
    val result by Kodein.global.instance<R>()
    return result
}

class KodeinModuleBuilder(private val ctx: Context) {

    fun build() = Kodein.Module("main") {
        bind<Bootstrap>() with singleton { Bootstrap() }
        bind<FsReader>() with singleton { FsReader() }
        bind<Comms>() with singleton { Comms() }
        bind<Login>() with singleton { Login() }
        bind<FsWriter>() with singleton { FsWriter() }
        bind<EventsDb>() with singleton { EventsDb(ctx) }
        bind<ColorFactory>() with singleton { ColorFactory(ctx) }
        bind<Context>() with provider { ctx }
        bind<Toaster>() with singleton { Toaster(ctx) }
    }
}