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
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fr_comm_details.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.*

class CommDetailsFragment : Fragment(R.layout.fr_comm_details) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val comm = getViewModel<CommViewModel>().comm
        title = comm.name
        if (comm.isAdmin) { edit_comm_fab.show() }
        else { edit_comm_fab.hide() }
        edit_comm_fab { nav(R.id.action_commDetailsFragment_to_commEditFragment) }
        name(comm.name)
        description(comm.desc)
    }
}
