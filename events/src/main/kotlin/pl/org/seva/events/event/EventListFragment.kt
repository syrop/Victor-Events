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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import pl.org.seva.events.R
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.login.Login
import pl.org.seva.events.main.extension.*
import pl.org.seva.events.main.init.instance

class EventListFragment : Fragment(R.layout.fr_event_list) {

    private val comms by instance<Comms>()
    private val events by instance<Events>()
    private val login by instance<Login>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val addEventFab = requireActivity().findViewById<FloatingActionButton>(R.id.add_event_fab)
        val eventsView = requireActivity().findViewById<RecyclerView>(R.id.events_view)
        val prompt = requireActivity().findViewById<View>(R.id.prompt)

        addEventFab { nav(R.id.action_eventsFragment_to_createEventFragment) }
        eventsView.setHasFixedSize(true)
        eventsView.layoutManager = LinearLayoutManager(context)
        eventsView.verticalDivider()
        eventsView.adapter = EventAdapter { position ->
            eventViewModel.value.withPosition(position)
            nav(R.id.action_eventsFragment_to_eventDetailsFragment)
        }

        (comms.updatedLiveData() + this) {
            if (comms.isAdminOfAny) addEventFab.show()
            else addEventFab.hide()
        }

        (events.updatedLiveData() + this) {
            if (events.isEmpty) {
                prompt.visibility = View.VISIBLE
                eventsView.visibility = View.GONE
            } else {
                prompt.visibility = View.GONE
                eventsView.visibility = View.VISIBLE
                checkNotNull(eventsView.adapter).notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.event_list, menu)
        menu.findItem(R.id.action_login).isVisible = !login.isLoggedIn
        menu.findItem(R.id.action_log_out).isVisible = login.isLoggedIn
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_login -> nav(R.id.action_eventsFragment_to_loginConfirmationFragment)
        R.id.action_log_out -> nav(R.id.action_eventsFragment_to_logOutConfirmationFragment)
        R.id.action_comms -> nav(R.id.action_eventsFragment_to_commListFragment)
        R.id.action_system_messages -> nav(R.id.action_eventsFragment_to_systemMessagesFragment)
        R.id.action_about -> nav(R.id.action_eventsFragment_to_aboutFragment)
        else -> super.onOptionsItemSelected(item)
    }
}
