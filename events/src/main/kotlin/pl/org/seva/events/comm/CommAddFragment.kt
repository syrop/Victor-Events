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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pl.org.seva.events.R
import pl.org.seva.events.login.Login
import pl.org.seva.events.login.LoginActivity
import pl.org.seva.events.main.extension.back
import pl.org.seva.events.main.extension.bold
import pl.org.seva.events.main.extension.boldAnnotated
import pl.org.seva.events.main.extension.longToast
import pl.org.seva.events.main.extension.onBack
import pl.org.seva.events.main.extension.plus
import pl.org.seva.events.main.extension.toast
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.EventsTheme
import pl.org.seva.events.main.ui.composeContent

@Suppress("DEPRECATION")
class CommAddFragment : Fragment() {

    private val comms by instance<Comms>()
    private val login by instance<Login>()

    private val commAddViewModel by activityViewModels<CommAddViewModel>()

    private val searchManager by lazy {
        requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        onBack { resetAndBack() }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ) = composeContent {
        val queryState by commAddViewModel.queryState.observeAsState(CommAddViewModel.QueryState.None)
        val snackbarHostState = remember { SnackbarHostState() }
        val defaultPrompt = stringResource(
                id = if (comms.isEmpty) R.string.add_comm_please_search_empty else R.string.add_comm_please_search,
        )

        LaunchedEffect(queryState) {
            snackbarHostState.currentSnackbarData?.dismiss()
            when (val result = queryState) {
                is CommAddViewModel.QueryState.Completed -> {
                    val comm = result.comm
                    if (comm.isDummy) {
                        val name = comm.originalName
                        if (login.isLoggedIn) {
                            val snackResult = snackbarHostState.showSnackbar(
                                    message = getString(R.string.add_comm_can_create),
                                    actionLabel = getString(R.string.add_comm_create),
                                    duration = SnackbarDuration.Indefinite,
                            )
                            if (snackResult == SnackbarResult.ActionPerformed) {
                                name.createJoinAndFinish()
                            }
                        } else {
                            val snackResult = snackbarHostState.showSnackbar(
                                    message = getString(R.string.add_comm_login_to_create),
                                    actionLabel = getString(R.string.add_comm_login),
                                    duration = SnackbarDuration.Indefinite,
                            )
                            if (snackResult == SnackbarResult.ActionPerformed) {
                                val intent = Intent(activity, LoginActivity::class.java)
                                        .putExtra(LoginActivity.COMMUNITY_NAME, name)
                                        .putExtra(LoginActivity.ACTION, LoginActivity.LOGIN)
                                startActivityForResult(intent, LOGIN_CREATE_COMM_REQUEST)
                            }
                        }
                    }
                }
                is CommAddViewModel.QueryState.Error -> {
                    longToast(result.errorMessage)
                }
                else -> Unit
            }
        }

        val foundComm = (queryState as? CommAddViewModel.QueryState.Completed)?.comm?.takeIf { !it.isDummy }
        val promptText = when (val result = queryState) {
            is CommAddViewModel.QueryState.Completed -> {
                if (result.comm.isDummy) {
                    stringResource(id = R.string.add_comm_not_found)
                            .boldAnnotated(NAME_PLACEHOLDER, result.comm.originalName)
                } else {
                    AnnotatedString("")
                }
            }
            else -> AnnotatedString(defaultPrompt)
        }

        CommAddScreen(
                inProgress = queryState is CommAddViewModel.QueryState.InProgress,
                foundCommName = foundComm?.name,
                foundCommColor = foundComm?.color ?: 0,
                promptText = promptText,
                snackbarHostState = snackbarHostState,
                onFoundCommClick = {
                    foundComm?.apply {
                        if (isMemberOf) {
                            toast(R.string.add_comm_already_a_member)
                        } else {
                            joinAndFinish()
                        }
                    }
                },
        )
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (commAddViewModel.commToCreate + this) { name ->
            if (!name.isNullOrEmpty()) {
                commAddViewModel.commToCreate.value = ""
                name.createJoinAndFinish()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        fun MenuItem.prepareSearchView() = with(actionView as SearchView) {
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        }

        menuInflater.inflate(R.menu.comm_add, menu)
        val searchMenuItem = menu.findItem(R.id.action_search)
        searchMenuItem.collapseActionView()
        searchMenuItem.prepareSearchView()
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> resetAndBack()
        else -> super.onOptionsItemSelected(item)
    }

    private fun String.createJoinAndFinish() {
        this@CommAddFragment.lifecycleScope.launch {
            comms joinNewCommunity this@createJoinAndFinish
            toast(
                    getString(R.string.add_comm_created)
                            .bold(NAME_PLACEHOLDER, this@createJoinAndFinish),
            )
            resetAndBack()
        }
    }

    private fun Comm.joinAndFinish() {
        lifecycleScope.launch {
            join()
            toast(
                    getString(R.string.add_comm_joined)
                            .bold(NAME_PLACEHOLDER, name),
            )
            resetAndBack()
        }
    }

    private fun resetAndBack(): Boolean {
        commAddViewModel.resetQuery()
        back()
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == LOGIN_CREATE_COMM_REQUEST) && (resultCode == Activity.RESULT_OK)) {
            checkNotNull(data).getStringExtra(LoginActivity.COMMUNITY_NAME)?.createJoinAndFinish()
        }
    }

    companion object {
        const val NAME_PLACEHOLDER = "[name]"
        const val LOGIN_CREATE_COMM_REQUEST = 0
    }
}

/**
 * Jetpack Compose screen replacing `res/layout/fr_comm_add.xml`.
 */
@Composable
fun CommAddScreen(
        inProgress: Boolean,
        foundCommName: String?,
        foundCommColor: Int,
        promptText: AnnotatedString,
        snackbarHostState: SnackbarHostState,
        onFoundCommClick: () -> Unit,
) {
    Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Box(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
        ) {
            when {
                inProgress -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                foundCommName != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        CommRow(
                                name = foundCommName,
                                color = foundCommColor,
                                onClick = onFoundCommClick,
                        )
                        HorizontalDivider()
                    }
                }
                else -> {
                    Text(
                            text = promptText,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommAddScreenPreview() {
    EventsTheme {
        CommAddScreen(
                inProgress = false,
                foundCommName = null,
                foundCommColor = 0,
                promptText = AnnotatedString(stringResource(id = R.string.add_comm_please_search)),
                snackbarHostState = remember { SnackbarHostState() },
                onFoundCommClick = {},
        )
    }
}
