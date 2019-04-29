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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fr_comm_details.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.*

class CommDetailsFragment : Fragment(R.layout.fr_comm_details) {

    private val vm by viewModel<CommViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val comm = vm.comm
        title = comm.name
        edit_comm_fab { nav(R.id.action_commDetailsFragment_to_commEditFragment) }
        name set comm.name
        (vm.name + this) { if (it == Comm.DUMMY_NAME) back() }
        (vm.desc + this) { desc set it }
        (vm.isAdmin + this) { if (it) edit_comm_fab.show() else edit_comm_fab.hide() }
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        fun addAdmin(): Boolean {
            nav(R.id.action_commDetailsFragment_to_adminAddFragment)
            return false
        }

        return when (item.itemId) {
            R.id.action_add_admin -> addAdmin()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.comm_details, menu)
        (vm.isAdmin + this) { menu.findItem(R.id.action_add_admin)?.isVisible = it }
    }
}
