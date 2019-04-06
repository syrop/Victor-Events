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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fr_comm_list.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.*

class CommListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflate(R.layout.fr_comm_list, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        fun refreshScreen() {
            if (comms.isEmpty()) {
                comms_view.visibility = View.GONE
                prompt.visibility = View.VISIBLE
            }
            else {
                comms_view.visibility = View.VISIBLE
                comms_view.adapter!!.notifyDataSetChanged()
                prompt.visibility = View.GONE
            }
        }

        super.onActivityCreated(savedInstanceState)

        comms_view.setHasFixedSize(true)
        comms_view.layoutManager = LinearLayoutManager(context)
        comms_view.adapter = CommAdapter { view ->
            val position = comms_view.getChildAdapterPosition(view)
            val commViewModel = provideViewModel<CommViewModel>()
            commViewModel.comm = comms[position]
            nav(R.id.action_commListFragment_to_commDetailsFragment)
        }
        comms_view.verticalDivider()
        comms_view.swipeListener { position ->
            val comm = comms[position]
            comm.leave()
            refreshScreen()
            Snackbar.make(
                    comms_view,
                    getString(R.string.comm_list_leave).bold(NAME_PLACEHOLDER, comm.name),
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.comm_list_undo) {
                        comm.join()
                        refreshScreen()
                    }
                    .show()
        }

        add_comm.setOnClickListener {
            nav(R.id.action_commListFragment_to_addCommFragment)
        }

        refreshScreen()
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
    }
}
