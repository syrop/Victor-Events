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
import kotlinx.android.synthetic.main.fragment_delete_comm.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.*

class DeleteCommFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflate(R.layout.fragment_delete_comm, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var pendingDeletion: Comm? = null

        fun deletePendingCommunity() {
            with (pendingDeletion) {
                if (this?.delete() != null) {
                    getString(R.string.delete_comm_confirmation)
                            .bold(NAME_PLACEHOLDER, name)
                            .toast()
                }
                else {
                    getString(R.string.delete_comm_no_changes).toast()
                }
            }

            back()
        }

        comms.isAdminOf.apply {
            comm_layout.visibility = View.VISIBLE
            comm_spinner.withObjects(
                    context!!,
                    listOf(getString(R.string.delete_comm_none))
                            .plus(map { it.name }).toTypedArray()) { position ->
                pendingDeletion = if (position == 0) null else this[position - 1]
            }
        }

        delete_button.setOnClickListener { deletePendingCommunity() }
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
    }
}