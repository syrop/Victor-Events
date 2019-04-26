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
import kotlinx.android.synthetic.main.fr_location_picker.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.*

class LocationPickerFragment : Fragment(R.layout.fr_location_picker) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        fun onLocationChanged(l: EventLocation?) {
            address set (l?.address ?: "")
            delete_location.visibility = if (l != null) View.VISIBLE else View.GONE
        }

        super.onActivityCreated(savedInstanceState)

        val viewModel = getViewModel<EventCreateViewModel>()
        delete_location { viewModel.location.value = null }
        (viewModel.location + this) { onLocationChanged(it) }

        createInteractiveMapHolder {
            onMapAvailable = {
                viewModel.informMarker(
                        this@LocationPickerFragment,
                        this@createInteractiveMapHolder)
            }
        }
    }
}
