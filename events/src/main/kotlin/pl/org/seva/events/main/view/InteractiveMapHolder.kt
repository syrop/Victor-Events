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

package pl.org.seva.events.main.view

import android.location.Geocoder
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import pl.org.seva.events.event.EventCreateViewModel
import pl.org.seva.events.event.EventLocation
import pl.org.seva.events.main.extension.getViewModel
import pl.org.seva.events.main.init.instance
import java.lang.Exception

class InteractiveMapHolder : MapHolder() {

    private lateinit var viewModel: EventCreateViewModel
    private val geocoder by instance<Geocoder>()

    override fun withFragment(fragment: Fragment): MapHolder {
        super.withFragment(fragment)
        viewModel = fragment.getViewModel()
        return this
    }

    override fun withMap(map: GoogleMap) {
        fun onMapLongClick(latLng: LatLng) {
            val address = try {
                with(geocoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        1)[0]) {
                        getAddressLine(maxAddressLineIndex)
                }
            } catch (ex: Exception) {
                DEFAULT_ADDRESS
            }
            viewModel.location.value = EventLocation(latLng, address)
        }

        super.withMap(map)
        with (map) {
            setOnMapLongClickListener { onMapLongClick(it) }
        }
    }

    companion object {
        const val DEFAULT_ADDRESS = ""
    }
}
