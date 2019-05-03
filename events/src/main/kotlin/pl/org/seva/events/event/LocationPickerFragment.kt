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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fr_location_picker.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.*

class LocationPickerFragment : Fragment(R.layout.fr_location_picker) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        fun onLocationChanged(l: EventLocation?) {
            address set (l?.address ?: "")
            if (l != null) delete_location_fab.show() else delete_location_fab.hide()
        }

        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        val viewModel = getViewModel<EventCreateViewModel>()
        delete_location_fab { viewModel.location.value = null }
        (viewModel.location + this) { onLocationChanged(it) }

        createInteractiveMapHolder(R.id.map) {
            onLocationSet = { viewModel.location.value = it }
            onMapAvailable = {
                (viewModel.location + this@LocationPickerFragment) { putMarker(it?.location) }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_ok -> back()
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.location_picker, menu)
    }
}
