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

import kotlinx.android.synthetic.main.fragment_events.*
import pl.org.seva.events.R
import pl.org.seva.events.comm.comms
import pl.org.seva.events.main.extension.navigate
import pl.org.seva.events.login.login
import pl.org.seva.events.main.extension.inflate

class EventsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflate(R.layout.fragment_events, container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (comms.isAdminOfAny) {
            add_event_fab.visibility = View.VISIBLE
        }

        add_event_fab.setOnClickListener {
            navigate(R.id.action_eventsFragment_to_createEventFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.events, menu)
        menu.findItem(R.id.action_login).isVisible = !login.isLoggedIn
        menu.findItem(R.id.action_leave_comm).isVisible = comms.isNotEmpty
        menu.findItem(R.id.action_delete_comm).isVisible = comms.isAdminOfAny
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_login -> navigate(R.id.action_eventsFragment_to_loginConfirmationFragment)
        R.id.action_add_comm -> navigate(R.id.action_eventsFragment_to_addCommFragment)
        R.id.action_leave_comm -> navigate(R.id.action_eventsFragment_to_leaveCommFragment)
        R.id.action_delete_comm -> navigate(R.id.action_eventsFragment_to_deleteCommFragment)
        R.id.action_system_messages -> navigate(R.id.action_eventsFragment_to_systemMessagesFragment)
        else -> super.onOptionsItemSelected(item)
    }
}
