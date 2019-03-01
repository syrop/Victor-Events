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
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_comm_edit.*
import kotlinx.android.synthetic.main.fragment_comm_list.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.bold
import pl.org.seva.events.main.extension.inflate
import pl.org.seva.events.main.extension.viewModel

class CommEditFragment : Fragment() {

    private val commViewModel by viewModel<CommListFragment.CommViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflate(R.layout.fragment_comm_edit, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        delete_comm_fab.setOnClickListener {
            val comm = commViewModel.comm
            comm.delete()
            Snackbar.make(
                    comms_view,
                    getString(R.string.comm_edit_deleted).bold(NAME_PLACEHOLDER, comm.name),
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.comm_edit_undelete) {
                    }
                    .show()
        }
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
    }
}
