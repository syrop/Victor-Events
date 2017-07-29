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

package pl.org.seva.events.view

import android.app.ProgressDialog
import android.app.SearchManager
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_search.*
import pl.org.seva.events.EventsApplication
import pl.org.seva.events.R
import pl.org.seva.events.model.Communities
import pl.org.seva.events.model.Community
import pl.org.seva.events.model.Login
import pl.org.seva.events.model.firebase.FbReader
import javax.inject.Inject

@Suppress("DEPRECATION")
class SearchActivity: AppCompatActivity() {

    @Inject
    lateinit var communities: Communities
    @Inject
    lateinit var fbReader: FbReader
    @Inject
    lateinit var login: Login

    private var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as EventsApplication).component.inject(this)
        setContentView(R.layout.activity_search)
        if (communities.empty) {
            prompt.setText(R.string.search_please_search_empty)
        }
        if (Intent.ACTION_SEARCH == intent.action) {
            search(intent.getStringExtra(SearchManager.QUERY))
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    private fun search(name: String) {
        progress = ProgressDialog.show(this, null, getString(R.string.search_searching))
        fbReader
                .findCommunity(name.toLowerCase())
                .subscribe {
                    if (it.empty) {
                        onCommunityNotFound(name)
                    } else {
                        onCommunityReceived(it)
                    }
                }
    }

    private fun onCommunityReceived(community: Community) {

    }

    private fun onCommunityNotFound(name: String) {
        prompt.text = name.notFound()
        if (login.isLoggedIn) {
            showCreateCommunitySnackbar(name)
        }
    }

    private fun showCreateCommunitySnackbar(name: String) {
        Snackbar.make(
                layout,
                R.string.search_can_create,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.search_create) { createCommunity(name) }
                .show()
    }

    private fun createCommunity(name: String) {
        communities.joinNewCommunity(name)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun String.notFound() : CharSequence {
        val str = getString(R.string.search_not_found)
        val idName = str.indexOf(NAME_PLACEHOLDER)
        val idEndName = idName + length
        val ssBuilder = SpannableStringBuilder(str.replace(NAME_PLACEHOLDER, this))
        val boldSpan = StyleSpan(Typeface.BOLD)
        ssBuilder.setSpan(boldSpan, idName, idEndName, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return ssBuilder
    }

    companion object {
        val NAME_PLACEHOLDER = "[name]"
    }
}
