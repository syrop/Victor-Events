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
import org.kodein.di.*
import pl.org.seva.events.BuildConfig
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.event.Events
import pl.org.seva.events.login.Login
import pl.org.seva.events.main.data.Permissions
import pl.org.seva.events.main.data.firestore.FsReader
import pl.org.seva.events.main.data.firestore.FsWriter
import pl.org.seva.events.main.data.db.EventsDb
import pl.org.seva.events.main.ui.ColorFactory
import pl.org.seva.events.message.Messages
import java.util.*
import java.util.logging.Logger

inline fun <reified R : Any> instance(tag: Any? = null) = kodein.instance<R>(tag)

inline fun <reified A : Any, reified T : Any> instance(tag: Any? = null, arg: A) =
        kodein.instance<A, T>(tag, arg = arg)

inline val <T> LazyDelegate<T>.value get() = provideDelegate(null, Build::ID).value

lateinit var kodein: DI

fun createKodein(ctx: Context) {
    kodein = DI {
        bindFactory { ctx: Context -> Bootstrap(ctx) }

        bindSingleton { Events(instance(), instance(), instance()) }
        bindSingleton {
            Comms(instance(), instance(), instance(), instance(), instance())
        }
        bindSingleton { Messages(instance()) }
        bindSingleton { instance<EventsDb>().eventsDao }
        bindSingleton { instance<EventsDb>().commsDao }
        bindSingleton { instance<EventsDb>().messagesDao }
        bindSingleton { EventsDb(ctx) }
        bindSingleton { FsReader(instance()) }
        bindSingleton { FsWriter() }
        bindSingleton { Login() }
        bindSingleton { ColorFactory(ctx) }
        bindMultiton { tag: String ->
            checkNotNull(Logger.getLogger(tag)).apply {
                if (!BuildConfig.DEBUG) {
                    setFilter { false }
                }
            }
        }
        bindSingleton { Permissions() }
        bindSingleton { Geocoder(ctx, Locale.getDefault()) }
    }
}
