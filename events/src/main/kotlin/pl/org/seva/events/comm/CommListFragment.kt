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
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.*
import pl.org.seva.events.main.init.instance

class CommListFragment : Fragment(R.layout.fr_comm_list) {

    private val comms by instance<Comms>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val commsView = requireActivity().findViewById<RecyclerView>(R.id.comms_view)
        commsView.setHasFixedSize(true)
        commsView.layoutManager = LinearLayoutManager(requireContext())
        commsView.verticalDivider()
        commsView.adapter = CommAdapter { position ->
            commViewModel.value.withPosition(position)
            nav(R.id.action_commListFragment_to_commDetailsFragment)
        }
        commsView.onSwipe { position ->
            val comm = comms[position]
            lifecycleScope.launch {
                comm.leave()
                Snackbar.make(
                        commsView,
                        getString(R.string.comm_list_leave).bold(NAME_PLACEHOLDER, comm.name),
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.comm_list_undo) { launch { comm.join() } }
                        .show()
            }
        }
        (requireActivity().findViewById<View>(R.id.add_comm_fab)) {
            nav(R.id.action_commListFragment_to_addCommFragment)
        }

        (comms.updatedLiveData() + this) {
            if (comms.isEmpty) {
                requireActivity().findViewById<View>(R.id.prompt).visibility = View.VISIBLE
                commsView.visibility = View.GONE
            }
            else {
                requireActivity().findViewById<View>(R.id.prompt).visibility = View.GONE
                commsView.visibility = View.VISIBLE
                checkNotNull(commsView.adapter).notifyDataSetChanged()
            }
        }
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
    }
}
