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
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_search.*
import pl.org.seva.events.EventsApplication
import pl.org.seva.events.R
import pl.org.seva.events.model.Communities
import pl.org.seva.events.model.Community
import pl.org.seva.events.model.firebase.FbReader
import javax.inject.Inject

class SearchActivity: AppCompatActivity() {

    @Inject
    lateinit var communities: Communities
    @Inject
    lateinit var fbReader: FbReader

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

    private fun search(query: String) {
        progress = ProgressDialog.show(this, null, getString(R.string.search_searching))
        fbReader
                .findCommunity(query.toLowerCase())
                .subscribe { onCommunityReceived(it) }
    }

    private fun onCommunityReceived(community: Community) {

    }
}
