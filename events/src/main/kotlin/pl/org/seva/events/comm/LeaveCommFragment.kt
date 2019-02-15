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

package pl.org.seva.events.comm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_leave_comm.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.bold
import pl.org.seva.events.main.extension.back
import pl.org.seva.events.main.extension.toast
import pl.org.seva.events.main.extension.withObjects

class LeaveCommFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_leave_comm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var pendingLeaving: Comm? = null

        fun leavePendingCommunity() {
            with (pendingLeaving) {
                if (this?.leave() != null) {
                    getString(R.string.leave_comm_confirmation)
                            .bold(NAME_PLACEHOLDER, name)
                            .toast()
                }
                else {
                    getString(R.string.leave_comm_no_changes).toast()
                }
            }

            back()
        }

        comms.names.apply {
            comm_layout.visibility = View.VISIBLE
            comm_spinner.withObjects(
                    context!!,
                    arrayOf(getString(R.string.leave_comm_none)).plus(this)) { position ->
                pendingLeaving = if (position == 0) null else comms[position - 1]
            }
        }

        leave_button.setOnClickListener { leavePendingCommunity() }
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
    }
}
