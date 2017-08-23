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

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import kotlinx.android.synthetic.main.activity_add_comm.*
import pl.org.seva.events.R
import pl.org.seva.events.model.Communities
import pl.org.seva.events.model.Community
import pl.org.seva.events.model.Login
import pl.org.seva.events.model.firebase.FbReader
import pl.org.seva.events.view.snackbar.longSnackbar

class AddCommActivity : AppCompatActivity(), KodeinGlobalAware {

    private val communities: Communities = instance()
    private val fbReader: FbReader = instance()
    private val login: Login = instance()

    private val searchManager get() = getSystemService(Context.SEARCH_SERVICE) as SearchManager
    private val isCommunitiesEmpty get() = communities.empty

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_comm)

        if (Intent.ACTION_SEARCH == intent.action) {
            search(intent.getStringExtra(SearchManager.QUERY))
        }
    }

    override fun onResume() {
        super.onResume()
        if (isCommunitiesEmpty) {
            emptyCommunitiesPrompt()
        } else {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
    }

    private fun emptyCommunitiesPrompt() {
        prompt.setText(R.string.add_comm_please_search_empty)
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
        setOnSearchClickListener { onSearchClicked() }
        setSearchableInfo(searchManager.getSearchableInfo(componentName))
        setOnCloseListener { onSearchViewClosed() }
    }

    private fun onSearchClicked() {
        prompt.visibility = View.GONE
    }

    private fun onSearchViewClosed(): Boolean {
        if (isCommunitiesEmpty) {
            prompt.visibility = View.VISIBLE
            emptyCommunitiesPrompt()
        }
        return false
    }

    private fun search(name: String) {
        prompt.visibility = View.GONE
        progress.visibility = View.VISIBLE
        fbReader.findCommunity(name.toLowerCase()) {
            if (empty) notFound() else found()
        }
    }

    private fun Community.found() {
        progress.visibility = View.GONE
        contacts.visibility = View.VISIBLE
    }

    private fun Community.notFound() {
        progress.visibility = View.GONE
        prompt.visibility = View.VISIBLE
        prompt.text = name.notFound()
        if (login.isLoggedIn) {
            name.showCreateSnackbar()
        }
    }

    private fun String.showCreateSnackbar() {
        longSnackbar {
            view = layout
            messageId = R.string.add_comm_can_create
            actionId = R.string.add_comm_create
        } show {
            createCommunity()
        }
    }

    private fun String.createCommunity() {
        communities.joinNewCommunity(this)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun String.notFound(): CharSequence = getString(R.string.add_comm_not_found).run {
        val idName = indexOf(NAME_PLACEHOLDER)
        val idEndName = idName + length
        val boldSpan = StyleSpan(Typeface.BOLD)
        SpannableStringBuilder(replace(NAME_PLACEHOLDER, this@notFound)).apply {
            setSpan(boldSpan, idName, idEndName, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    }

    companion object {
        val NAME_PLACEHOLDER = "[name]"
    }
}
