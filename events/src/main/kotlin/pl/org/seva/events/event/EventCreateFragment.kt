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
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fr_event_create.*
import pl.org.seva.events.R
import pl.org.seva.events.comm.comms
import pl.org.seva.events.main.*
import pl.org.seva.events.main.extension.*
import java.time.LocalDate
import java.time.LocalTime

class EventCreateFragment : Fragment() {

    private val model by viewModel<EventCreateViewModel>()

    private val mapHolder by lazy {
        createMapHolder {
            checkLocationPermission = this@EventCreateFragment::checkLocationPermission
            onMapAvailable = {
                model.informMarker(this@EventCreateFragment, this@createMapHolder)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflate(R.layout.fr_event_create, container)

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fun showTimePicker() = TimePickerFragment().show(fragmentManager!!, TIME_PICKER_TAG)
        fun showDatePicker() = DatePickerFragment().show(fragmentManager!!, DATE_PICKER_TAG)
        fun showLocationPicker() = nav(R.id.action_createEventFragment_to_locationPickerFragment)

        fun onTimeChanged(t: LocalTime?) = time.setText(if (t == null) "" else "${t.hour}:${t.minute}")
        fun onDateChanged(d: LocalDate?) = date.setText(if (d == null) "" else "${d.year}-${d.monthValue}-${d.dayOfMonth}")

        fun onLocationChanged(l: EventLocation?) {
            (l?.address?.apply { mapHolder } ?: "").also { addressLine ->
                address.setText(addressLine) }
            map_container.visibility = if (l == null) View.INVISIBLE else View.VISIBLE
        }

        name += model.name + this
        time.setOnClickListener { showTimePicker() }
        date.setOnClickListener { showDatePicker() }
        address.setOnClickListener { showLocationPicker() }
        description += model.description + this

        model.time(this) { onTimeChanged(it) }
        model.date(this) { onDateChanged(it) }
        model.location(this) { onLocationChanged(it) }

        comms.namesIsAdminOf.apply {
            model.comm.value = get(0)
            if (size > 1) {
                comm_layout.visibility = View.VISIBLE
                comm.setText(get(0))
                comm += model.comm + this@EventCreateFragment
                comm.setOnClickListener {
                    AlertDialog.Builder(context!!)
                            .setItems(this) { dialog, which ->
                                comm.setText(get(which))
                                dialog.dismiss()
                            }
                            .show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.comm_edit, menu)
    }

    private fun checkLocationPermission(onGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            onGranted.invoke()
        }
        else {
            requestPermissions(
                    Permissions.DEFAULT_PERMISSION_REQUEST_ID,
                    arrayOf(Permissions.PermissionRequest(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            onGranted = onGranted)))
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            requests: Array<String>,
            grantResults: IntArray) =
            permissions.onRequestPermissionsResult(requestCode, requests, grantResults)

    companion object {
        const val TIME_PICKER_TAG = "timePicker"
        const val DATE_PICKER_TAG = "datePicker"
    }
}
