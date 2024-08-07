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
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.*

class LocationPickerFragment : Fragment(R.layout.fr_location_picker) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        val deleteLocactionFab = requireActivity().findViewById<FloatingActionButton>(R.id.delete_location_fab)

        fun onLocationChanged(l: EventLocation?) {
            requireActivity().findViewById<TextView>(R.id.address) set (l?.address ?: "")
            if (l != null) deleteLocactionFab.show() else deleteLocactionFab.hide()
        }

        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        val viewModel = eventCreateViewModel.value
        deleteLocactionFab { viewModel.location.value = null }
        (viewModel.location + this) { onLocationChanged(it) }

        createInteractiveMapHolder(R.id.map) {
            prefs = prefs(SHARED_PREFERENCES_TAG)
            onLocationSet = { viewModel.location.value = it }
        }.also { holder ->
            (holder.liveMap + this) { map ->
                enableMyLocationOnResume(map)
                (viewModel.location + this@LocationPickerFragment) { holder.markPosition(it?.location) }
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

    companion object {
        private const val SHARED_PREFERENCES_TAG = "fragment_location_picker_preferences"
    }
}
