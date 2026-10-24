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

package pl.org.seva.events.main.ui

import android.location.Geocoder
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import pl.org.seva.events.event.EventLocation
import pl.org.seva.events.main.init.instance

class InteractiveMapHolder : MapHolder() {

    private val geocoder by instance<Geocoder>()
    lateinit var onLocationSet: (EventLocation) -> Unit

    override fun withMap(map: GoogleMap) {
        fun onMapLongClick(latLng: LatLng) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<android.location.Address>) {
                                val address = addresses.firstOrNull()?.run {
                                    getAddressLine(maxAddressLineIndex)
                                } ?: DEFAULT_ADDRESS
                                Handler(Looper.getMainLooper()).post {
                                    onLocationSet(EventLocation(latLng, address))
                                }
                            }

                            override fun onError(errorMessage: String?) {
                                Handler(Looper.getMainLooper()).post {
                                    onLocationSet(EventLocation(latLng, DEFAULT_ADDRESS))
                                }
                            }
                        },
                )
            } else {
                val address = try {
                    @Suppress("DEPRECATION")
                    with(
                            requireNotNull(
                                    geocoder.getFromLocation(
                                            latLng.latitude,
                                            latLng.longitude,
                                            1,
                                    ),
                            )[0],
                    ) {
                        getAddressLine(maxAddressLineIndex)
                    }
                } catch (_: Exception) {
                    DEFAULT_ADDRESS
                }
                onLocationSet(EventLocation(latLng, address))
            }
        }

        super.withMap(map)
        map.setOnMapLongClickListener { onMapLongClick(it) }
    }

    companion object {
        const val DEFAULT_ADDRESS = ""
    }
}
