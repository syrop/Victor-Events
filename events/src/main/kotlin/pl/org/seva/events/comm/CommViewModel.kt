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

package pl.org.seva.events.comm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import pl.org.seva.events.main.init.instance

class CommViewModel(private val state: SavedStateHandle = SavedStateHandle()) : ViewModel() {

    private val comms by instance<Comms>()

    var comm = Comm.DUMMY
        private set(value) {
            field = value
            reset()
        }

    val name = MutableStateFlow<String?>(null)
    val desc = MutableStateFlow<String?>(null)
    val isAdmin = MutableStateFlow<Boolean?>(null)

    init {
        viewModelScope.launch {
            comms.updates.collect { update() }
        }
        val position = state.get<Int>(COMM_POSITION) ?: -1
        if (position >= 0) {
            comm = comms[position]
        }
    }

    fun withPosition(position: Int) {
        comm = comms[position]
        state.set(COMM_POSITION, position)
    }

    fun update() {
        comm = comms[comm.name]
        reset()
    }

    fun reset() {
        name.value = comm.name
        desc.value = comm.desc
        isAdmin.value = comm.isAdmin
    }

    companion object {
        const val COMM_POSITION = "comm_position"
    }
}
