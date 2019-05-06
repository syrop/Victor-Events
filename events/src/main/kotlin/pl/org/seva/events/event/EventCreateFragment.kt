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

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fr_event_create.*
import pl.org.seva.events.R
import pl.org.seva.events.comm.comms
import pl.org.seva.events.main.extension.*
import pl.org.seva.events.main.model.Permissions
import pl.org.seva.events.main.model.permissions
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class EventCreateFragment : Fragment(R.layout.fr_event_create) {

    private val vm by viewModel<EventCreateViewModel>()

    private val mapHolder by lazy {
        createMapHolder(R.id.map) {
            checkLocationPermission = this@EventCreateFragment::checkLocationPermission
            onMapAvailable = {
                (vm.location + this@EventCreateFragment) { putMarker(it?.location) }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        fun showTimePicker() = TimePickerFragment().show(fragmentManager!!, TIME_PICKER_TAG)
        fun showDatePicker() = DatePickerFragment().show(fragmentManager!!, DATE_PICKER_TAG)
        fun showLocationPicker() = nav(R.id.action_createEventFragment_to_locationPickerFragment)

        fun onTimeChanged(t: LocalTime?) = time set (t?.toString() ?: "")
        fun onDateChanged(d: LocalDate?) = date set (d?.toString() ?: "")

        fun onLocationChanged(l: EventLocation?) {
            address set l?.address
            if (l != null) {
                map_container.visibility = View.VISIBLE
                mapHolder
            }
            else { map_container.visibility = View.GONE }
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
                comm_layout.visibility = View.VISIBLE
                comm set get(0)
                comm backWith (vm.comm + this@EventCreateFragment)
                comm {
                    AlertDialog.Builder(context!!)
                            .setItems(this) { dialog, which ->
                                comm set get(which)
                                dialog.dismiss()
                            }
                            .show()
                }
            }
        }
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
            events add Event(
                    comm = vm.comm.value!!,
                    name = vm.name.value!!,
                    time = LocalDateTime.of(vm.date.value!!, vm.time.value!!),
                    location = vm.location.value?.location,
                    address = vm.location.value?.address,
                    desc = vm.desc.value)
            vm.clear()
            back()
            return true
        }

        return when (item.itemId) {
            R.id.action_ok -> confirm()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.event_create, menu)
    }

    private fun checkLocationPermission(onGranted: () -> Unit) {
        if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) { onGranted() }
        else {
            request(
                    Permissions.DEFAULT_PERMISSION_REQUEST_ID,
                    arrayOf(Permissions.PermissionRequest(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            onGranted = onGranted)))
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            requests: Array<String>,
            grantResults: IntArray) =
            permissions.onRequestPermissionsResult(this, requestCode, requests, grantResults)

    companion object {
        private const val TIME_PICKER_TAG = "time_picker"
        private const val DATE_PICKER_TAG = "date_picker"
    }
}
