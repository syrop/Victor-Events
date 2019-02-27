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

package pl.org.seva.events.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_messages.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.inflate
import pl.org.seva.events.main.extension.swipeListener

class MessagesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflate(R.layout.fragment_messages, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        fun refreshScreen() {
            if (messages.isEmpty()) {
                messages_view.visibility = View.GONE
                prompt.visibility = View.VISIBLE
            }
            else {
                messages_view.visibility = View.VISIBLE
                messages_view.adapter!!.notifyDataSetChanged()
                prompt.visibility = View.GONE
            }
        }

        super.onActivityCreated(savedInstanceState)

        messages_view.setHasFixedSize(true)
        messages_view.layoutManager = LinearLayoutManager(context)
        messages_view.adapter = MessageAdapter()
        messages_view.addItemDecoration(DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL))
        messages_view.swipeListener { position ->
            messages.delete(position)
            refreshScreen()
        }
        refreshScreen()
    }
}
