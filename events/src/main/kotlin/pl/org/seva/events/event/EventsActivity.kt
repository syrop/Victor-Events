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

package pl.org.seva.events.event

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View

import kotlinx.android.synthetic.main.activity_events.*
import pl.org.seva.events.R
import pl.org.seva.events.community.communities
import pl.org.seva.events.community.startAddCommActivity
import pl.org.seva.events.login.login
import pl.org.seva.events.login.startLoginConfirmationActivity

class EventsActivity : AppCompatActivity() {

    private val communities = communities()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)
        if (!communities.isAdminOfAny) {
            add_fab.visibility = View.VISIBLE
        }

        add_fab.setOnClickListener { startCreateEventActivity() }
    }

    override fun onResume() {
        super.onResume()
        if (communities.isEmpty) { startAddCommActivity() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.events, menu)
        menu.findItem(R.id.action_login).isVisible = !login().isLoggedIn
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_login -> startLoginConfirmationActivity()
        R.id.action_seek_community -> startAddCommActivity()
        else -> super.onOptionsItemSelected(item)
    }
}
