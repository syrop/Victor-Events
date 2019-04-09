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
import kotlinx.android.synthetic.main.fr_comm_delete.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.*

class CommDeleteFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflate(R.layout.fr_comm_delete, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val comm = getViewModel<CommViewModel>().comm
        title = getString(R.string.comm_delete_title).replace(NAME_PLACEHOLDER, comm.name)
        prompt.text = getString(R.string.comm_delete_prompt).bold(NAME_PLACEHOLDER, comm.name)
        ok.setOnClickListener {
            comm.delete()
            getString(R.string.comm_delete_toast).bold(NAME_PLACEHOLDER, comm.name).longToast()
            back()
        }
        cancel.setOnClickListener {
            back()
        }
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
    }
}
