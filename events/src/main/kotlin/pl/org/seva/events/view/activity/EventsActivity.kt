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

package pl.org.seva.events.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance

import kotlinx.android.synthetic.main.activity_events.*
import pl.org.seva.events.R
import pl.org.seva.events.data.Communities
import pl.org.seva.events.data.Login
import pl.org.seva.events.data.model.Community

class EventsActivity : AppCompatActivity(), KodeinGlobalAware {

    private val login: Login = instance()
    private val communities: Communities = instance()

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
        if (communities.empty) startAddCommActivity() else Unit
    }

    private fun startAddCommActivity() =
            Intent(this, AddCommActivity::class.java)
                .startActivityForResult(ADD_COMMUNITY_REQUEST_CODE)

    private fun startCreateEventActivity() =
            Intent(this, CreateEventActivity::class.java).startActivity()

    private fun startLoginQuestionActivity() =
            Intent(this, LoginConfirmationActivity::class.java).startActivity()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.events, menu)
        menu.findItem(R.id.action_login).isVisible = !login.isLoggedIn
        return true
    }

    @JvmName("intentStartActivity")
    private fun Intent.startActivity() {
        startActivity(this)
    }

    @JvmName("intentStartActivityForResult")
    private fun Intent.startActivityForResult(requestCode: Int) {
        startActivityForResult(this, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != ADD_COMMUNITY_REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return
        }
        val addedCommunity : Community = data.getParcelableExtra(COMMUNITY_TAG)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_login -> { startLoginQuestionActivity(); true }
        R.id.action_seek_community -> { startAddCommActivity(); true }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        val ADD_COMMUNITY_REQUEST_CODE = 0
        val COMMUNITY_TAG = "community"
    }
}
