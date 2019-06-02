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

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateVMFactory
import androidx.lifecycle.ViewModel
import androidx.navigation.navGraphViewModels
import pl.org.seva.events.R

val Fragment.eventViewModel get() =
    navGraphViewModels<EventViewModel>(R.id.nav_graph) { SavedStateVMFactory(activity!!) }

class EventViewModel(private val state: SavedStateHandle) : ViewModel() {

    lateinit var event: Event
        private set

    init {
        val position = state.get<Int>(EVENT_POSITION) ?: -1
        if (position >= 0) {
            event = events[position]
        }
    }

    fun withPosition(position: Int) {
        event = events[position]
        state.set(EVENT_POSITION, position)
    }

    companion object {
        const val EVENT_POSITION = "event_position"
    }
}
