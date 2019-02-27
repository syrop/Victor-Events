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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_comm_add.*
import pl.org.seva.events.R
import pl.org.seva.events.main.fs.fsReader
import pl.org.seva.events.login.LoginActivity
import pl.org.seva.events.login.isLoggedIn
import pl.org.seva.events.main.EventsViewModel
import pl.org.seva.events.main.extension.*
import pl.org.seva.events.main.ui.*

class CommAddFragment : Fragment() {

    private val eventsModel by viewModel<EventsViewModel>()

    private val searchManager by lazy {
        activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
    }

    private lateinit var adapter: CommAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)  =
            inflate(R.layout.fragment_comm_add, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fun search(name: String) {
            fun Comm.found() {
                progress.visibility = View.GONE
                recycler.visibility = View.VISIBLE
                adapter = CommAdapter(this) {
                    if (isMemberOf) {
                        getString(R.string.add_comm_already_a_member).toast()
                    }
                    else {
                        joinAndFinish()
                    }
                }
                recycler.addItemDecoration(DividerItemDecoration(activity!!, DividerItemDecoration.VERTICAL))
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
                prompt.text = getString(R.string.add_comm_not_found).bold(NAME_PLACEHOLDER, name)
                if (isLoggedIn) {
                    showCreateCommunitySnackbar(name)
                } else {
                    showLoginToCreateSnackbar(name)
                }
            }

            prompt.visibility = View.GONE
            progress.visibility = View.VISIBLE
            fsReader.findCommunity(lifecycle, name) {
                if (dummy) notFound() else found()
            }
        }

        prompt.setText(if (comms.isEmpty) R.string.add_comm_please_search_empty else
            R.string.add_comm_please_search)
        eventsModel.query.observe(this) { name ->
            if (name.isNotEmpty()) {
                eventsModel.query.value = ""
                search(name)
            }
        }
        eventsModel.commToCreate.observe(this) { name ->
            if (!name.isNullOrEmpty()) {
                eventsModel.commToCreate.value = ""
                name.createJoinAndFinish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        fun MenuItem.prepareSearchView() = with (actionView as SearchView) {
            setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
        }

        menuInflater.inflate(R.menu.add_comm, menu)
        val searchMenuItem = menu.findItem(R.id.action_search)
        searchMenuItem.collapseActionView()
        searchMenuItem.prepareSearchView()
    }

    private fun String.createJoinAndFinish() {
        comms joinNewCommunity this
        getString(R.string.add_comm_created)
                .bold(NAME_PLACEHOLDER, this)
                .toast()
        back()
    }

    private fun Comm.joinAndFinish() {
        join()
        getString(R.string.add_comm_joined)
                .bold(NAME_PLACEHOLDER, name)
                .toast()
        back()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_CREATE_COMM_REQUEST && resultCode == Activity.RESULT_OK) {
            data!!.getStringExtra(LoginActivity.COMMUNITY_NAME)?.createJoinAndFinish()
        }
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
        const val LOGIN_CREATE_COMM_REQUEST = 0
    }
}
