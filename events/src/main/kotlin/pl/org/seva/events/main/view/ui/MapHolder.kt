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

package pl.org.seva.events.main.view.ui

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.org.seva.events.main.extension.googleMap

open class MapHolder {
    private var map: GoogleMap? = null
    var checkLocationPermission: ((onGranted: () -> Unit) -> Unit)? = null
    var onMapAvailable: ((GoogleMap) -> Unit)? = null
    var prefs: () -> SharedPreferences? = { null }

    infix fun withFragment(pair: Pair<Fragment, Int>): MapHolder {
        val (fragment, id) = pair
        GlobalScope.launch(Dispatchers.Main) {
            val map = fragment.googleMap(id)
            this@MapHolder withMap map
        }
        return this
    }

    @SuppressLint("MissingPermission")
    protected open infix fun withMap(map: GoogleMap) {
        with (map) {
            this@MapHolder.map = this
            val cameraUpdate = prefs()?.let { prefs ->
                val latLng = LatLng(
                        prefs.getFloat(LAT_PROPERTY, DEFAULT_LAT.toFloat()).toDouble(),
                        prefs.getFloat(LON_PROPERTY, DEFAULT_LON.toFloat()).toDouble())
                val zoom = prefs.getFloat(ZOOM_PROPERTY, DEFAULT_ZOOM)
                CameraUpdateFactory.newLatLngZoom(latLng, zoom)
            } ?: CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LAT, DEFAULT_LON), DEFAULT_ZOOM)
            moveCamera(cameraUpdate)
            setOnCameraIdleListener {
                prefs()?.edit {
                    val position = map.cameraPosition
                    putFloat(ZOOM_PROPERTY, position.zoom)
                    putFloat(LAT_PROPERTY, position.target.latitude.toFloat())
                    putFloat(LON_PROPERTY, position.target.longitude.toFloat())
                }
            }
            onMapAvailable?.invoke(this)
            checkLocationPermission?.invoke {
                isMyLocationEnabled = true
            }
        }
    }

    fun putMarker(latLng: LatLng?) {
        with (map!!) {
            clear()
            latLng ?: return
            addMarker(MarkerOptions().position(latLng))
                    .setIcon(BitmapDescriptorFactory.defaultMarker(0f))
        }
    }

    companion object {
        private const val DEFAULT_LAT = 51.1
        private const val DEFAULT_LON = 17.033333
        private const val DEFAULT_ZOOM = 15f

        private const val ZOOM_PROPERTY = "zoom_property"
        private const val LAT_PROPERTY = "latitude_property"
        private const val LON_PROPERTY = "longitude_property"
    }
}
