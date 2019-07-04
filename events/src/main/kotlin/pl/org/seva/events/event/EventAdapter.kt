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

package pl.org.seva.events.event

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_event.view.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.inflate
import pl.org.seva.events.main.extension.onClick
import pl.org.seva.events.main.init.instance

class EventAdapter(private val onClick: (Int) -> Unit) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    private val events by instance<Events>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(parent.inflate(R.layout.row_event), onClick)

    override fun getItemCount() = events.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        events[position].apply {
            holder.name.text = name
            holder.time.text = time.toString()
        }
    }

    class ViewHolder(view: View, onClick: (Int) -> Unit) : RecyclerView.ViewHolder(view) {

        init { view onClick { onClick(adapterPosition) } }

        val name: TextView = view.name
        val time: TextView = view.time
    }
}
