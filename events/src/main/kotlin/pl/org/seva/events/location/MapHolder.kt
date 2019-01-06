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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import pl.org.seva.events.R

fun Fragment.createMapHolder(f: MapHolder.() -> Unit = {}): MapHolder =
        MapHolder().apply(f) withFragment this

open class MapHolder {
    private var map: GoogleMap? = null
    var checkLocationPermission: ((onGranted: () -> Unit) -> Unit)? = null

    open infix fun withFragment(fragment: Fragment): MapHolder {
        with (fragment) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync { map -> this@MapHolder withMap map }
        }
        return this
    }

    @SuppressLint("MissingPermission")
    protected open infix fun withMap(map: GoogleMap) {
        with (map) {
            this@MapHolder.map = this
            moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LAT, DEFAULT_LON), DEFAULT_ZOOM))
            checkLocationPermission?.invoke {
                isMyLocationEnabled = true
            }
        }
    }

    companion object {
        private const val DEFAULT_LAT = 51.1
        private const val DEFAULT_LON = 17.033333
        private const val DEFAULT_ZOOM = 15f
    }
}
