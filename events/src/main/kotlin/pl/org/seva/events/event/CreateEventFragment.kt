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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_create_event.*
import pl.org.seva.events.R
import java.time.LocalDate
import java.time.LocalTime

class CreateEventFragment : Fragment() {

    private lateinit var viewModel: DateTimeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_event, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fun showTimePicker() = TimePickerFragment().show(fragmentManager, TIME_PICKER_TAG)
        fun showDatePicker() = DatePickerFragment().show(fragmentManager, DATE_PICKER_TAG)
        fun onTimeChanged(t: LocalTime) = time.setText("${t.hour}:${t.minute}")
        fun onDateChanged(d: LocalDate) = date.setText("${d.year}-${d.monthValue}-${d.dayOfMonth}")

        viewModel = ViewModelProviders.of(activity!!).get(DateTimeViewModel::class.java)
        time.setOnClickListener { showTimePicker() }
        date.setOnClickListener { showDatePicker() }
        location.setOnClickListener {}
        viewModel.time.observe(this, Observer<LocalTime> { onTimeChanged(it) })
        viewModel.date.observe(this, Observer<LocalDate> { onDateChanged(it) })
    }

    companion object {
        const val TIME_PICKER_TAG = "timePicker"
        const val DATE_PICKER_TAG = "datePicker"
    }
}
