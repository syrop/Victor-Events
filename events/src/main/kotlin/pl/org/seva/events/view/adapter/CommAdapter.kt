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
 */

package pl.org.seva.events.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import pl.org.seva.events.R
import pl.org.seva.events.data.Communities
import pl.org.seva.events.view.adapter.viewholder.CommViewHolder

class CommAdapter : RecyclerView.Adapter<CommViewHolder>(), KodeinGlobalAware {

    private val communities: Communities = instance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LayoutInflater.from(parent.context).inflate(R.layout.row_community, parent, false).let {
        CommViewHolder(it)
    }

    override fun getItemCount() = communities.size

    override fun onBindViewHolder(holder: CommViewHolder, position: Int) = with (holder) {
        communities[position].let {
            communityName.text = it.name
            iconText.text = it.name.substring(0, 1)
            iconProfile.setColorFilter(it.color)
        }
    }
}