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

package pl.org.seva.events.event

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View

import kotlinx.android.synthetic.main.activity_events.*
import pl.org.seva.events.R
import pl.org.seva.events.community.AddCommActivity
import pl.org.seva.events.data.communities
import pl.org.seva.events.data.login
import pl.org.seva.events.login.LoginConfirmationActivity

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
        addCommunityIfEmpty()
    }

    private fun addCommunityIfEmpty() {
        if (communities.empty) startAddCommActivity()
    }

    private fun startAddCommActivity() =
            startActivity(Intent(this, AddCommActivity::class.java))

    private fun startCreateEventActivity() =
            startActivity(Intent(this, CreateEventActivity::class.java))

    private fun startLoginQuestionActivity() =
            startActivity(Intent(this, LoginConfirmationActivity::class.java))

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.events, menu)
        menu.findItem(R.id.action_login).isVisible = !login().isLoggedIn
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_login -> { startLoginQuestionActivity(); true }
        R.id.action_seek_community -> { startAddCommActivity(); true }
        else -> super.onOptionsItemSelected(item)
    }
}
