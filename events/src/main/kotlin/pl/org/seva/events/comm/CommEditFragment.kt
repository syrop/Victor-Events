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
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.*

class CommEditFragment : Fragment(R.layout.fr_comm_edit) {

    private val vm by commViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        (requireActivity().findViewById<View>(R.id.delete_comm_fab)) { nav(R.id.action_commEditFragment_to_commDeleteFragment) }
        requireActivity().findViewById<TextView>(R.id.name) set vm.comm.name
        (vm.name + this) { if (it == Comm.DUMMY_NAME) back() }
        requireActivity().findViewById<TextInputEditText>(R.id.desc) backWith (vm.desc + this)
        onBack { onBackOrHomePressed() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        fun updateAndFinish(): Boolean {
            with(vm.comm.copy(desc = vm.desc.value ?: "")) {
                update()
                vm.update()
                toast(getString(R.string.comm_edit_updated)
                        .bold(NAME_PLACEHOLDER, name))
                back()
            }
            return true
        }

        return when (item.itemId) {
            R.id.action_ok -> updateAndFinish()
            android.R.id.home -> onBackOrHomePressed()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onBackOrHomePressed(): Boolean {
        vm.reset()
        back()
        return true
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.comm_edit, menu)
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
    }
}
