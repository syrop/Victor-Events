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
import android.widget.TextView
import androidx.fragment.app.Fragment
import pl.org.seva.events.R
import pl.org.seva.events.main.data.firestore.FsWriter
import pl.org.seva.events.main.extension.back
import pl.org.seva.events.main.extension.toast
import pl.org.seva.events.main.init.instance

class AdminAddFragment : Fragment(R.layout.fr_admin_add) {

    private val fsWriter by instance<FsWriter>()

    private val vm by commViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.admin_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        fun confirm(): Boolean {
            fsWriter.grantAdmin(vm.comm, requireActivity().findViewById<TextView>(R.id.address).text.toString())
            toast(R.string.admin_add_toast)
            back()
            return true
        }

        return when (item.itemId) {
            R.id.action_ok -> confirm()
            else -> super.onOptionsItemSelected(item)
        }
    }
}
