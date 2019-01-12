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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_location_picker.*
import pl.org.seva.events.R
import pl.org.seva.events.location.InteractiveMapHolder
import pl.org.seva.events.location.createInteractiveMapHolder

class LocationPickerFragment : Fragment() {

    private lateinit var mapHolder: InteractiveMapHolder
    private lateinit var viewModel: CreateEventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_location_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fun onLocationChanged(l: EventLocation?) {
            address.setText(l?.address ?: "")
            delete_location.isEnabled = l != null
        }

        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(CreateEventViewModel::class.java)
        delete_location.setOnClickListener {
            viewModel.location.value = null
        }
        viewModel.location.observe(this, Observer { onLocationChanged(it) })
        mapHolder = createInteractiveMapHolder {
            onMapAvailable = {
                viewModel.observeLocation(this@LocationPickerFragment, this@createInteractiveMapHolder)
            }
        }
    }
}