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
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import pl.org.seva.events.R
import pl.org.seva.events.login.Login
import pl.org.seva.events.login.LoginActivity
import pl.org.seva.events.main.extension.*
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.*

class CommAddFragment : Fragment(R.layout.fr_comm_add) {

    private val comms by instance<Comms>()
    private val login by instance<Login>()

    private val commAddViewModel by activityViewModels<CommAddViewModel>()

    private val searchManager by lazy {
        requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
    }

    private var snackbar: Snackbar? = null

    private lateinit var adapter: CommAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        val progress = requireActivity().findViewById<View>(R.id.progress)
        val recycler = requireActivity().findViewById<RecyclerView>(R.id.recycler)
        val prompt = requireActivity().findViewById<TextView>(R.id.prompt)

        fun Comm.found() {
            progress.visibility = View.GONE
            recycler.visibility = View.VISIBLE
            adapter = FixedCommAdapter(this) {
                if (isMemberOf) {
                    toast(R.string.add_comm_already_a_member)
                }
                else { joinAndFinish() }
            }
            recycler.verticalDivider()
            recycler.adapter = adapter
            recycler.layoutManager = LinearLayoutManager(context)
        }

        fun Comm.notFound() {
            fun showCreateCommunitySnackbar(name: String) {
                snackbar = permanentSnackbar {
                    view = requireActivity().findViewById(R.id.layout)
                    message = R.string.add_comm_can_create
                    action = R.string.add_comm_create
                } show {
                    name.createJoinAndFinish()
                }
            }

            fun showLoginToCreateSnackbar(name: String) {
                fun loginToCreateComm(name: String) {
                    val intent = Intent(activity, LoginActivity::class.java)
                            .putExtra(LoginActivity.COMMUNITY_NAME, name)
                            .putExtra(LoginActivity.ACTION, LoginActivity.LOGIN)
                    startActivityForResult(intent, LOGIN_CREATE_COMM_REQUEST)
                }

                snackbar = permanentSnackbar {
                    view = requireActivity().findViewById(R.id.layout)
                    message = R.string.add_comm_login_to_create
                    action = R.string.add_comm_login
                } show {
                    loginToCreateComm(name)
                }
            }
            progress.visibility = View.GONE
            prompt.visibility = View.VISIBLE
            prompt.text = getString(R.string.add_comm_not_found).bold(NAME_PLACEHOLDER, originalName)
            if (login.isLoggedIn) showCreateCommunitySnackbar(originalName)
            else showLoginToCreateSnackbar(originalName)
        }

        super.onActivityCreated(savedInstanceState)
        prompt.setText(if (comms.isEmpty) R.string.add_comm_please_search_empty else
            R.string.add_comm_please_search)
        (commAddViewModel.queryState + this) { result ->
            snackbar?.dismiss()
            when (result) {
                is CommAddViewModel.QueryState.InProgress -> {
                    recycler.visibility = View.GONE
                    prompt.visibility = View.GONE
                    progress.visibility = View.VISIBLE
                }
                is CommAddViewModel.QueryState.Completed -> result.comm.apply {
                    if (isDummy) notFound() else found()
                }
                is CommAddViewModel.QueryState.Error -> {
                    recycler.visibility = View.GONE
                    prompt.visibility = View.VISIBLE
                    progress.visibility = View.GONE
                    longToast(result.errorMessage)
                }
                else -> throw IllegalStateException("")
            }
        }
        (commAddViewModel.commToCreate + this) { name ->
            if (!name.isNullOrEmpty()) {
                commAddViewModel.commToCreate.value = ""
                name.createJoinAndFinish()
            }
        }

        onBack { resetAndBack() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        fun MenuItem.prepareSearchView() = with (actionView as SearchView) {
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        }

        menuInflater.inflate(R.menu.comm_add, menu)
        val searchMenuItem = menu.findItem(R.id.action_search)
        searchMenuItem.collapseActionView()
        searchMenuItem.prepareSearchView()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> resetAndBack()
        else -> super.onOptionsItemSelected(item)
    }

    private fun String.createJoinAndFinish() {
        this@CommAddFragment.lifecycleScope.launch {
            comms joinNewCommunity this@createJoinAndFinish
            toast(getString(R.string.add_comm_created)
                    .bold(NAME_PLACEHOLDER, this@createJoinAndFinish))
            resetAndBack()
        }
    }

    private fun Comm.joinAndFinish() {
        lifecycleScope.launch {
            join()
            toast(getString(R.string.add_comm_joined)
                    .bold(NAME_PLACEHOLDER, name))
            resetAndBack()
        }
    }

    private fun resetAndBack(): Boolean {
        commAddViewModel.resetQuery()
        back()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_CREATE_COMM_REQUEST && resultCode == Activity.RESULT_OK) {
            checkNotNull(data).getStringExtra(LoginActivity.COMMUNITY_NAME)?.createJoinAndFinish()
        }
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
        const val LOGIN_CREATE_COMM_REQUEST = 0
    }
}
