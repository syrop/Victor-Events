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
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.onSwipe
import pl.org.seva.events.main.extension.plus
import pl.org.seva.events.main.extension.verticalDivider
import pl.org.seva.events.main.init.instance

class MessagesFragment : Fragment(R.layout.fr_messages) {

    private val messages by instance<Messages>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val messagesView = requireActivity().findViewById<RecyclerView>(R.id.messages_view)
        val prompt = requireActivity().findViewById<View>(R.id.prompt)

        messagesView.setHasFixedSize(true)
        messagesView.layoutManager = LinearLayoutManager(context)
        messagesView.adapter = MessagesAdapter()
        messagesView.verticalDivider()
        messagesView.onSwipe { position ->
                lifecycleScope.launch { messages[position].delete() }
        }

        (messages.updatedLiveData() + this) {
            if (messages.isEmpty()) {
                messagesView.visibility = View.GONE
                prompt.visibility = View.VISIBLE
            }
            else {
                messagesView.visibility = View.VISIBLE
                checkNotNull(messagesView.adapter).notifyDataSetChanged()
                prompt.visibility = View.GONE
            }
        }
    }
}
