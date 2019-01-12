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

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import pl.org.seva.events.location.MapHolder
import java.time.LocalDate
import java.time.LocalTime

class CreateEventViewModel : ViewModel() {

    val comm by lazy {
        MutableLiveData<String>()
    }

    val time by lazy {
        MutableLiveData<LocalTime>()
    }

    val date by lazy {
        MutableLiveData<LocalDate>()
    }

    val location: MutableLiveData<EventLocation?> by lazy {
        MutableLiveData<EventLocation?>()
    }

    fun observeLocation(owner: LifecycleOwner, mapHolder: MapHolder) {
        location.observe(owner, Observer { mapHolder.putMarker(it?.location) })
    }
}