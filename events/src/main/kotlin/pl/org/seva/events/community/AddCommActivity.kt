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

package pl.org.seva.events.community

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
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_comm.*
import pl.org.seva.events.R
import pl.org.seva.events.data.firebase.fbReader
import pl.org.seva.events.login.login
import pl.org.seva.events.login.LoginActivity
import pl.org.seva.events.main.ui.boldSection
import pl.org.seva.events.main.ui.DividerItemDecoration
import pl.org.seva.events.main.ui.longSnackbar
import pl.org.seva.events.main.ui.permanentSnackbar

class AddCommActivity : AppCompatActivity() {

    private val communities = communities()

    private val searchManager get() = getSystemService(Context.SEARCH_SERVICE) as SearchManager

    private lateinit var adapter: CommAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_comm)

        if (Intent.ACTION_SEARCH == intent.action) {
            search(intent.getStringExtra(SearchManager.QUERY))
        }
        if (communities.empty) {
            prompt.setText(R.string.add_comm_please_search_empty)
        } else {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
    }

    private fun communitiesNotFoundPrompt() {

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
        fun Community.found() {
            fun Community.joinAndFinish() {
                communities join this
                finish()
            }

            progress.visibility = View.GONE
            recycler.visibility = View.VISIBLE
            adapter = CommAdapter(this) { joinAndFinish() }
            recycler.addItemDecoration(DividerItemDecoration(this@AddCommActivity))
            recycler.adapter = adapter
        }

        fun notFound() {
            fun showCreateCommunitySnackbar(name: String) {
                longSnackbar {
                    view = layout
                    message = R.string.add_comm_can_create
                    action = R.string.add_comm_create
                } show {
                    name.createJoinAndFinish()
                }
            }

            fun showLoginToCreateSnackbar(name: String) {
                fun loginToCreateComm(name: String) {
                    Intent(this, LoginActivity::class.java)
                            .putExtra(LoginActivity.COMMUNITY_NAME, name)
                            .putExtra(LoginActivity.ACTION, LoginActivity.LOGIN).let {
                        startActivityForResult(it, LOGIN_CREATE_COMM_REQUEST)
                    }
                }

                permanentSnackbar {
                    view = layout
                    message = R.string.add_comm_login_to_create
                    action = R.string.add_comm_login
                } show {
                    loginToCreateComm(name)
                }
            }

            progress.visibility = View.GONE
            prompt.visibility = View.VISIBLE
            prompt.text = getString(R.string.add_comm_not_found).boldSection(NAME_PLACEHOLDER, name)
            if (login().isLoggedIn) {
                showCreateCommunitySnackbar(name)
            } else {
                showLoginToCreateSnackbar(name)
            }
        }

        prompt.visibility = View.GONE
        progress.visibility = View.VISIBLE
        fbReader().findCommunity(name) {
            if (empty) notFound() else found()
        }
    }

    private fun String.createJoinAndFinish() {
        fun String.created() =
                getString(R.string.add_comm_created).boldSection(NAME_PLACEHOLDER, this)

        communities joinNewCommunity this
        Toast.makeText(this@AddCommActivity, created(), Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_CREATE_COMM_REQUEST && resultCode == Activity.RESULT_OK) {
            data.getStringExtra(LoginActivity.COMMUNITY_NAME)?.createJoinAndFinish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> { finish(); true }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
        const val LOGIN_CREATE_COMM_REQUEST = 0
    }
}
