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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.LocalTime

class EventCreateViewModel : ViewModel() {

    val comm by lazy { MutableLiveData<String?>() }

    val name by lazy { MutableLiveData<String?>() }

    val time by lazy { MutableLiveData<LocalTime?>() }

    val date by lazy { MutableLiveData<LocalDate?>() }

    val location by lazy { MutableLiveData<EventLocation?>() }

    val desc by lazy { MutableLiveData<String?>() }

    val isFilledIn
        get() = eventData.any { !it.value?.toString().isNullOrEmpty() }

    fun clear() = eventData.onEach { it.value = null }

    private val eventData by lazy {
        listOf(name, time, date, location, desc)
    }
}
