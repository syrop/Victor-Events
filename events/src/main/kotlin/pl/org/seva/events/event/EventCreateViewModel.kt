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

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalTime

class EventCreateViewModel : ViewModel() {

    val comm = MutableStateFlow<String?>(null)
    val name = MutableStateFlow<String?>(null)
    val time = MutableStateFlow<LocalTime?>(null)
    val date = MutableStateFlow<LocalDate?>(null)
    val location = MutableStateFlow<EventLocation?>(null)
    val desc = MutableStateFlow<String?>(null)

    val isFilledIn
        get() = eventData.any { !it.value?.toString().isNullOrEmpty() }

    fun clear() {
        name.value = null
        time.value = null
        date.value = null
        location.value = null
        desc.value = null
    }

    private val eventData: List<MutableStateFlow<out Any?>> by lazy {
        listOf(name, time, date, location, desc)
    }
}
