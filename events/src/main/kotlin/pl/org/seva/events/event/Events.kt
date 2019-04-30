/*
 * Copyright (C) 2019 Wiktor Nizio
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

package pl.org.seva.events.event

import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.model.LiveRepository
import pl.org.seva.events.main.model.db.db
import pl.org.seva.events.main.model.fs.fsWriter
import pl.org.seva.events.main.model.io

val events by instance<Events>()

class Events : LiveRepository() {
    private val eventsCache = mutableListOf<Event>()
    private val eventDao by lazy { db.eventDao }

    val size get() = eventsCache.size

    infix fun add(event: Event) {
        eventsCache.add(event)
        io { eventDao add event }
        fsWriter add event
    }

    operator fun get(index: Int) = eventsCache[index]
}
