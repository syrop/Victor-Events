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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import pl.org.seva.events.R
import pl.org.seva.events.main.data.Permissions
import pl.org.seva.events.main.extension.*
import pl.org.seva.events.main.init.instance

class EventDetailsFragment : Fragment(R.layout.fr_event_details) {

    private val permissions by instance<Permissions>()

    private val address by lazy { requireActivity().findViewById<TextInputEditText>(R.id.address) }
    private val date by lazy { requireActivity().findViewById<TextInputEditText>(R.id.date) }
    private val time by lazy { requireActivity().findViewById<TextInputEditText>(R.id.time) }
    private val name by lazy { requireActivity().findViewById<TextInputEditText>(R.id.name) }
    private val desc by lazy { requireActivity().findViewById<TextInputEditText>(R.id.desc) }
    private val comm by lazy { requireActivity().findViewById<TextInputEditText>(R.id.comm) }
    private val mapContainer by lazy { requireActivity().findViewById<View>(R.id.map_container) }
    private val addressLayout by lazy { requireActivity().findViewById<View>(R.id.address_layout) }
    private val descLayout by lazy { requireActivity().findViewById<View>(R.id.desc_layout) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val event = eventViewModel.value.event
        title = event.name
        comm set event.comm
        name set event.name
        time set event.time.toLocalTime().toString()
        date set event.time.toLocalDate().toString()
        with(event.desc) {
            if (isNullOrEmpty()) descLayout.visibility = View.GONE
            else desc set this
        }
        with (event.address) {
            if (isNullOrEmpty()) addressLayout.visibility = View.GONE
            else address set this
        }
        if (event.location != null) {
            mapContainer.visibility = View.VISIBLE
            createMapHolder(R.id.map) {
                requestLocationPermission = this@EventDetailsFragment::requestLocationPermission
            }.also { holder ->
                (holder.liveMap + this) {
                    holder.markPosition(event.location)
                    enableMyLocationOnResume(it)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            requests: Array<String>,
            grantResults: IntArray) =
            permissions.onRequestPermissionsResult(this, requestCode, requests, grantResults)
}
