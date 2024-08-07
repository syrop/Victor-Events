/*
 * Copyright (C) 2017 Wiktor Nizio
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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import pl.org.seva.events.R
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.main.data.Permissions
import pl.org.seva.events.main.extension.*
import pl.org.seva.events.main.init.instance
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class EventCreateFragment : Fragment(R.layout.fr_event_create) {

    private val events by instance<Events>()
    private val comms by instance<Comms>()
    private val permissions by instance<Permissions>()

    private val address by lazy { requireActivity().findViewById<TextInputEditText>(R.id.address) }
    private val date by lazy { requireActivity().findViewById<TextInputEditText>(R.id.date) }
    private val time by lazy { requireActivity().findViewById<TextInputEditText>(R.id.time) }
    private val name by lazy { requireActivity().findViewById<TextInputEditText>(R.id.name) }
    private val desc by lazy { requireActivity().findViewById<TextInputEditText>(R.id.desc) }
    private val comm by lazy { requireActivity().findViewById<TextInputEditText>(R.id.comm) }
    private val mapContainer by lazy { requireActivity().findViewById<View>(R.id.map_container) }

    private val vm by eventCreateViewModel

    private val mapHolder by lazy {
        createMapHolder(R.id.map) {
            requestLocationPermission = this@EventCreateFragment::requestLocationPermission
        }.also { holder ->
            (holder.liveMap + this) { map ->
                enableMyLocationOnResume(map)
                (vm.location + this) { holder.markPosition(it?.location) }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        fun showTimePicker() = nav(R.id.action_eventCreateFragment_to_timePicker)
        fun showDatePicker() = nav(R.id.action_eventCreateFragment_to_datePicker)
        fun showLocationPicker() = nav(R.id.action_createEventFragment_to_locationPickerFragment)

        fun onTimeChanged(t: LocalTime?) = time set (t?.toString() ?: "")
        fun onDateChanged(d: LocalDate?) = date set (d?.toString() ?: "")

        fun onLocationChanged(l: EventLocation?) {
            address set l?.address
            if (l != null) {
                mapContainer.visibility = View.VISIBLE
                mapHolder
            }
            else { mapContainer.visibility = View.GONE }
        }

        name backWith (vm.name + this)
        time { showTimePicker() }
        date { showDatePicker() }
        address { showLocationPicker() }
        desc backWith (vm.desc + this)

        (vm.time + this) { onTimeChanged(it) }
        (vm.date + this) { onDateChanged(it) }
        (vm.location + this) { onLocationChanged(it) }

        comms.namesIsAdminOf.apply {
            if (isEmpty()) { back() }
            vm.comm.value = get(0)
            if (size > 1) {
                requireActivity().findViewById<View>(R.id.comm_layout).visibility = View.VISIBLE
                comm set get(0)
                comm backWith (vm.comm + this@EventCreateFragment)
                comm {
                    AlertDialog.Builder(requireContext())
                            .setItems(this) { dialog, which ->
                                comm set get(which)
                                dialog.dismiss()
                            }
                            .show()
                }
            }
        }

        onBack { onBackOrHomePressed() }
    }

    private fun onBackOrHomePressed(): Boolean {
        if (vm.isFilledIn) {
            question(
                    message = getString(R.string.main_activity_dismiss_event),
                    yes = {
                        vm.clear()
                        back()
                    })
        } else back()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        fun confirm(): Boolean {
            if (listOf(vm.comm to comm, vm.name to name, vm.date to date, vm.time to time)
                    .map {
                        it.first.value.run {
                            if (this is String?) isNullOrEmpty()
                            else this == null
                        }.apply {
                            it.second.error = if (this) getString(R.string.create_event_cant_be_empty)
                            else null
                        }
                    }.any { it }) return true
            lifecycleScope.launch {
                events.add(Event(
                        comm = checkNotNull(vm.comm.value),
                        name = checkNotNull(vm.name.value),
                        time = LocalDateTime.of(checkNotNull(vm.date.value), checkNotNull(vm.time.value)),
                        location = vm.location.value?.location,
                        address = vm.location.value?.address,
                        desc = vm.desc.value, ))
                vm.clear()
                back()
            }
            return true
        }

        return when (item.itemId) {
            R.id.action_ok -> confirm()
            android.R.id.home -> onBackOrHomePressed()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.event_create, menu)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            requests: Array<String>,
            grantResults: IntArray,
    ) =
            permissions.onRequestPermissionsResult(this, requestCode, requests, grantResults)
}
