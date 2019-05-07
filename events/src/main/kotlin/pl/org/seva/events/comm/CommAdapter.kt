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
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.row_comm.view.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.inflate
import pl.org.seva.events.main.extension.onClick

open class CommAdapter(private val onClick: (Int) -> Unit) : RecyclerView.Adapter<CommAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(parent.inflate(R.layout.row_comm), onClick)

    override fun getItemCount() = comms.size

    protected open fun getItem(position: Int): Comm = comms[position]

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).apply {
            holder.communityName.text = name
            holder.iconText.text = name.substring(0, 1)
            holder.iconProfile.setImageResource(R.drawable.bg_circle)
            holder.iconProfile.setColorFilter(color)
        }
    }

    class ViewHolder(view: View, onClick: (Int) -> Unit) : RecyclerView.ViewHolder(view) {

        init { view onClick { onClick(adapterPosition) } }

        val communityName: TextView = view.comm
        val iconProfile: ImageView = view.icon_profile
        val iconText: TextView = view.icon_text
    }
}
