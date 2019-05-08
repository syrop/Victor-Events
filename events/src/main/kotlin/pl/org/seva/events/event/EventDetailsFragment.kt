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
import kotlinx.android.synthetic.main.fr_event_details.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.*
import pl.org.seva.events.main.model.permissions

class EventDetailsFragment : Fragment(R.layout.fr_event_details) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val event = getViewModel<EventViewModel>().event
        title = event.name
        comm set event.comm
        name set event.name
        time set event.time.toLocalTime().toString()
        date set event.time.toLocalDate().toString()
        with(event.desc) {
            if (isNullOrEmpty()) { desc_layout.visibility = View.GONE }
            else { desc set this }
        }
        with (event.address) {
            if (isNullOrEmpty()) { address_layout.visibility = View.GONE }
            else { address set this }
        }
        if (event.location != null) {
            map_container.visibility = View.VISIBLE
            @Suppress("ReplaceSingleLineLet")
            createMapHolder(R.id.map) {
                requestLocationPermission = this@EventDetailsFragment::requestLocationPermission
            }.let { holder ->
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
