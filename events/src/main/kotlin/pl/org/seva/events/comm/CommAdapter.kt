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

package pl.org.seva.events.comm

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import pl.org.seva.events.R

class CommAdapter(
        private val comm: Community,
        private var listener: (Community.() -> Unit)? = null) : RecyclerView.Adapter<CommViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            CommViewHolder(parent.inflate()) {
                onClick()
            }

    private fun ViewGroup.inflate() =
            LayoutInflater.from(context).inflate(R.layout.row_community, this, false)

    override fun getItemCount() = 1

    private fun onClick() = listener?.invoke(comm)

    override fun onBindViewHolder(holder: CommViewHolder, position: Int) = with (holder) {
        communityName.text = comm.name
        iconText.text = comm.name.substring(0, 1)
        iconProfile.setImageResource(R.drawable.bg_circle)
        iconProfile.setColorFilter(comm.color)
    }
}
