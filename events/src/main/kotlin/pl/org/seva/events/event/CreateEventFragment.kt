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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_create_event.*
import pl.org.seva.events.R
import pl.org.seva.events.comm.comms
import pl.org.seva.events.location.MapHolder
import pl.org.seva.events.location.createMapHolder
import pl.org.seva.events.main.Permissions
import pl.org.seva.events.main.navigate
import pl.org.seva.events.main.permissions
import pl.org.seva.events.main.requestPermissions
import java.time.LocalDate
import java.time.LocalTime

class CreateEventFragment : Fragment() {

    private lateinit var model: CreateEventViewModel

    private lateinit var mapHolder: MapHolder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_event, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fun showTimePicker() = TimePickerFragment().show(fragmentManager, TIME_PICKER_TAG)
        fun showDatePicker() = DatePickerFragment().show(fragmentManager, DATE_PICKER_TAG)
        fun showLocationPicker() = navigate(R.id.action_createEventFragment_to_locationPickerFragment)
        fun onTimeChanged(t: LocalTime) = time.setText("${t.hour}:${t.minute}")
        fun onDateChanged(d: LocalDate) = date.setText("${d.year}-${d.monthValue}-${d.dayOfMonth}")
        fun onLocationChanged(l: EventLocation?) {
            location.setText(l?.address ?: "")
            map_container.visibility = if (l == null) View.INVISIBLE else View.VISIBLE
        }

        model = ViewModelProviders.of(activity!!).get(CreateEventViewModel::class.java)
        time.setOnClickListener { showTimePicker() }
        date.setOnClickListener { showDatePicker() }
        location.setOnClickListener { showLocationPicker() }
        model.time.observe(this, Observer<LocalTime> { onTimeChanged(it) })
        model.date.observe(this, Observer<LocalDate> { onDateChanged(it) })
        model.location.observe(this, Observer { onLocationChanged(it) })

        mapHolder = createMapHolder {
            checkLocationPermission = this@CreateEventFragment::checkLocationPermission
            onMapAvailable = {
                model.observeLocation(this@CreateEventFragment, this@createMapHolder)
            }
        }

        with (comms.namesIsAdminOf) {
            model.comm.value = get(0)
            if (size > 1) {
                comm_layout.visibility = View.VISIBLE
                comm_spinner.adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, this)
                comm_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        model.comm.value = get(position)
                    }
                }
            }
        }
    }

    private fun checkLocationPermission(onGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            onGranted.invoke()
        } else {
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
