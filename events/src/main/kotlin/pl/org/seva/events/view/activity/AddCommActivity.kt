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
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import kotlinx.android.synthetic.main.activity_add_comm.*
import pl.org.seva.events.R
import pl.org.seva.events.data.Communities
import pl.org.seva.events.data.model.Community
import pl.org.seva.events.data.Login
import pl.org.seva.events.data.firebase.FbReader
import pl.org.seva.events.view.adapter.CommAdapter
import pl.org.seva.events.view.bold
import pl.org.seva.events.view.decoration.DividerItemDecoration
import pl.org.seva.events.view.snackbar.longSnackbar
import pl.org.seva.events.view.snackbar.permanentSnackbar

class AddCommActivity : AppCompatActivity(), KodeinGlobalAware {

    private val communities: Communities = instance()
    private val fbReader: FbReader = instance()
    private val login: Login = instance()

    private val searchManager get() = getSystemService(Context.SEARCH_SERVICE) as SearchManager

    private lateinit var adapter: CommAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_comm)

        if (Intent.ACTION_SEARCH == intent.action) {
            search(intent.getStringExtra(SearchManager.QUERY))
        }
        if (communities.empty) {
            communitiesNotFoundPrompt()
        } else {
            showBackArrow()
        }
    }

    private fun communitiesNotFoundPrompt() {
        prompt.setText(R.string.add_comm_please_search_empty)
    }

    private fun showBackArrow() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    override fun onBackPressed() = if (!communities.empty) super.onBackPressed() else Unit

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY).trim { it <= ' ' }
            search(query)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_community, menu)
        val searchMenuItem = menu.findItem(R.id.action_search)
        searchMenuItem.collapseActionView()
        searchMenuItem.prepareSearchView()
        return true
    }

    private fun MenuItem.prepareSearchView() = with (actionView as SearchView) {
        setSearchableInfo(searchManager.getSearchableInfo(componentName))
        setOnSearchClickListener { onSearchClicked() }
        setOnCloseListener { onSearchViewClosed() }
    }

    private fun onSearchClicked() {
        prompt.visibility = View.GONE
    }

    private fun onSearchViewClosed(): Boolean {
        if (communities.empty) {
            prompt.visibility = View.VISIBLE
            communitiesNotFoundPrompt()
        }
        return false
    }

    private fun search(name: String) {
        prompt.visibility = View.GONE
        progress.visibility = View.VISIBLE
        fbReader.findCommunity(name) {
            if (empty) notFound() else found()
        }
    }

    private fun Community.found() {
        progress.visibility = View.GONE
        recycler.visibility = View.VISIBLE
        adapter = CommAdapter(this) { joinAndFinish() }
        recycler.addItemDecoration(DividerItemDecoration(this@AddCommActivity))
        recycler.adapter = adapter
    }

    private fun Community.notFound() {
        progress.visibility = View.GONE
        prompt.visibility = View.VISIBLE
        prompt.text = name.commNotFound()
        if (login.isLoggedIn) {
            name.showCreateCommunitySnackbar()
        } else {
            name.showLoginToCreateSnackbar()
        }
    }

    private fun String.showCreateCommunitySnackbar() {
        longSnackbar {
            view = layout
            message = R.string.add_comm_can_create
            action = R.string.add_comm_create
        } show {
            createJoinAndFinish()
        }
    }

    private fun String.showLoginToCreateSnackbar() {
        permanentSnackbar {
            view = layout
            message = R.string.add_comm_login_to_create
            action = R.string.add_comm_login
        } show {
            loginToCreateComm()
        }
    }

    private fun Community.joinAndFinish() {
        join()
        finish()
    }

    private fun String.createJoinAndFinish() {
        joinNewCommunity()
        finish()
    }

    private fun String.loginToCreateComm() {
        Intent(this@AddCommActivity, LoginActivity::class.java)
                .putExtra(LoginActivity.COMMUNITY_NAME, this).let {
            startActivityForResult(it, LOGIN_CREATE_COMM_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_CREATE_COMM_REQUEST && resultCode == Activity.RESULT_OK) {
            data.getStringExtra(LoginActivity.COMMUNITY_NAME)?.createJoinAndFinish()
        }
    }

    private fun String.joinNewCommunity() = communities joinNewCommunity this

    private fun Community.join() = communities join this

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> { finish(); true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun String.commNotFound(): CharSequence =
            getString(R.string.add_comm_not_found).bold(NAME_PLACEHOLDER, this)

    companion object {
        val NAME_PLACEHOLDER = "[name]"
        val LOGIN_CREATE_COMM_REQUEST = 0
    }
}
