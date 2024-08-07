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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.*

class CommDeleteFragment : Fragment(R.layout.fr_comm_delete) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val comm = commViewModel.value.comm
        title = getString(R.string.comm_delete_title).replace(NAME_PLACEHOLDER, comm.name)
        requireActivity().findViewById<TextView>(R.id.prompt).text = getString(R.string.comm_delete_prompt).bold(NAME_PLACEHOLDER, comm.name)
        (requireActivity().findViewById<View>(R.id.ok)) {
            lifecycleScope.launch {
                comm.delete()
                longToast(getString(R.string.comm_delete_toast).bold(NAME_PLACEHOLDER, comm.name))
                back()
            }
        }
        (requireActivity().findViewById<View>(R.id.cancel)) {
            back()
        }
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
    }
}
