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

package pl.org.seva.events.comm

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_add_comm.*
import pl.org.seva.events.R
import pl.org.seva.events.main.fs.fsReader
import pl.org.seva.events.login.login
import pl.org.seva.events.login.LoginActivity
import pl.org.seva.events.main.EventsViewModel
import pl.org.seva.events.main.ui.boldSection
import pl.org.seva.events.main.observe
import pl.org.seva.events.main.ui.DividerItemDecoration
import pl.org.seva.events.main.ui.longSnackbar
import pl.org.seva.events.main.ui.permanentSnackbar

class AddCommFragment : Fragment() {

    private lateinit var eventsModel: EventsViewModel

    private val searchManager get() =
        activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager

    private lateinit var adapter: CommAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_add_comm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        eventsModel = ViewModelProviders.of(activity!!).get(EventsViewModel::class.java)
        if (communities.isEmpty) {
            prompt.setText(R.string.add_comm_please_search_empty)
        }
        eventsModel.query.observe(this) { query ->
            if (!query.isEmpty()) {
                eventsModel.query.value = ""
                search(query)
            }
        }
        eventsModel.commToCreate.observe(this) {
            if (!it.isNullOrEmpty()) {
                eventsModel.commToCreate.value = ""
                it.createJoinAndFinish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun communitiesNotFoundPrompt() {

    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.add_community, menu)
        val searchMenuItem = menu.findItem(R.id.action_search)
        searchMenuItem.collapseActionView()
        searchMenuItem.prepareSearchView()
    }

    private fun MenuItem.prepareSearchView() = with (actionView as SearchView) {
        setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
        setOnSearchClickListener { onSearchClicked() }
        setOnCloseListener { onSearchViewClosed() }
    }

    private fun onSearchClicked() {
        prompt.visibility = View.GONE
    }

    private fun onSearchViewClosed(): Boolean {
        if (communities.isEmpty) {
            prompt.visibility = View.VISIBLE
            communitiesNotFoundPrompt()
        }
        return false
    }

    private fun search(name: String) {
        fun Comm.found() {
            fun Comm.joinAndFinish() {
                join()
                findNavController().popBackStack()
            }
            progress.visibility = View.GONE
            recycler.visibility = View.VISIBLE
            adapter = CommAdapter(this) { joinAndFinish() }
            recycler.addItemDecoration(DividerItemDecoration(activity!!))
            recycler.adapter = adapter
            recycler.layoutManager = LinearLayoutManager(context)
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
                    startActivityForResult(Intent(activity, LoginActivity::class.java)
                        .putExtra(LoginActivity.COMMUNITY_NAME, name)
                        .putExtra(LoginActivity.ACTION, LoginActivity.LOGIN), LOGIN_CREATE_COMM_REQUEST)
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
        fsReader.findCommunity(lifecycle, name) {
            if (empty) notFound() else found()
        }
    }

    private fun String.createJoinAndFinish() {
        fun String.created() =
                getString(R.string.add_comm_created).boldSection(NAME_PLACEHOLDER, this)

        communities joinNewCommunity this
        Toast.makeText(activity, created(), Toast.LENGTH_LONG).show()
        findNavController().popBackStack()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_CREATE_COMM_REQUEST && resultCode == Activity.RESULT_OK) {
            data!!.getStringExtra(LoginActivity.COMMUNITY_NAME)?.createJoinAndFinish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            findNavController().popBackStack()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
        const val LOGIN_CREATE_COMM_REQUEST = 0
    }
}
