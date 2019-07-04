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

package pl.org.seva.events.main.init

import android.content.Context
import android.location.Geocoder
import android.os.Build
import org.kodein.di.Kodein
import org.kodein.di.KodeinProperty
import org.kodein.di.conf.global
import org.kodein.di.generic.*
import pl.org.seva.events.BuildConfig
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.comm.CommsDao
import pl.org.seva.events.event.Events
import pl.org.seva.events.event.EventsDao
import pl.org.seva.events.login.Login
import pl.org.seva.events.main.data.Permissions
import pl.org.seva.events.main.data.firestore.FsReader
import pl.org.seva.events.main.data.firestore.FsWriter
import pl.org.seva.events.main.data.db.EventsDb
import pl.org.seva.events.main.ui.ColorFactory
import pl.org.seva.events.main.ui.Toaster
import pl.org.seva.events.message.Messages
import pl.org.seva.events.message.MessagesDao
import java.util.*
import java.util.logging.Logger

val Context.module get() = KodeinModuleBuilder(this).build()

inline fun <reified R : Any> instance(tag: Any? = null) = Kodein.global.instance<R>(tag)

inline fun <reified A, reified T : Any> instance(tag: Any? = null, arg: A) =
        Kodein.global.instance<A, T>(tag, arg = arg)

inline val <T> KodeinProperty<T>.value get() = provideDelegate(null, Build::ID).value

class KodeinModuleBuilder(private val ctx: Context) {

    fun build() = Kodein.Module(MAIN_MODULE_NAME) {
        bind<Bootstrap>() with factory { ctx: Context -> Bootstrap(ctx) }
        bind<FsReader>() with singleton { FsReader() }
        bind<Comms>() with singleton { Comms() }
        bind<Events>() with singleton { Events() }
        bind<Login>() with singleton { Login() }
        bind<FsWriter>() with singleton { FsWriter() }
        bind<EventsDb>() with singleton { EventsDb(ctx) }
        bind<ColorFactory>() with singleton { ColorFactory(ctx) }
        bind<Toaster>() with singleton { Toaster(ctx) }
        bind<Logger>() with multiton { tag: String ->
            Logger.getLogger(tag)!!.apply {
                if (!BuildConfig.DEBUG) {
                    @Suppress("UsePropertyAccessSyntax")
                    setFilter { false }
                }
            }
        }
        bind<Permissions>() with singleton { Permissions() }
        bind<Geocoder>() with singleton { Geocoder(ctx, Locale.getDefault()) }
        bind<Messages>() with singleton { Messages() }
        bind<EventsDao>() with singleton { instance<EventsDb>().eventsDao }
        bind<CommsDao>() with singleton { instance<EventsDb>().commsDao }
        bind<MessagesDao>() with singleton { instance<EventsDb>().messagesDao }
    }

    companion object {
        const val MAIN_MODULE_NAME = "main"
    }
}
