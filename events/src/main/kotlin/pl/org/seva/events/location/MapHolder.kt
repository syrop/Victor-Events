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

package pl.org.seva.events.location

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import pl.org.seva.events.R

fun Fragment.createMapHolder(f: MapHolder.() -> Unit = {}): MapHolder = MapHolder().apply(f).also {
    val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync { map -> it withMap map }
}

open class MapHolder {
    private var map: GoogleMap? = null
    var checkLocationPermission: ((onGranted: () -> Unit) -> Unit)? = null

    infix fun withMap(map: GoogleMap) = map.onReady()

    @SuppressLint("MissingPermission")
    private fun GoogleMap.onReady() {

        this@MapHolder.map = apply {
            checkLocationPermission?.invoke {
                isMyLocationEnabled = true
            }
        }
    }
}
