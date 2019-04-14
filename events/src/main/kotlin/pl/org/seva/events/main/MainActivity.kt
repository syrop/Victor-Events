package pl.org.seva.events.main

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.act_main.*
import pl.org.seva.events.R
import pl.org.seva.events.comm.CommAddFragment
import pl.org.seva.events.comm.CommViewModel
import pl.org.seva.events.event.EventCreateViewModel
import pl.org.seva.events.main.extension.viewModel
import pl.org.seva.events.login.LoginActivity
import pl.org.seva.events.main.data.EventsViewModel
import pl.org.seva.events.main.extension.question

class MainActivity : AppCompatActivity() {

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    private val model by viewModel<EventsViewModel>()

    private val createEventsViewModel by viewModel<EventCreateViewModel>()

    private val commViewModel by viewModel<CommViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)
        setSupportActionBar(toolbar)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onBackPressed() {
        if (!backOrHome()) {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> backOrHome()
        else -> false
    } || super.onOptionsItemSelected(item)

    private fun backOrHome(): Boolean {
        fun showDismissEventDialog() =
                if (createEventsViewModel.isFilledIn) {
                    question(
                            message = getString(R.string.events_activity_dismiss_event),
                            yes = {
                                createEventsViewModel.clear()
                                navController.popBackStack()
                            })
                    true
                }
                else false

        fun resetCommViewModel(): Boolean {
            commViewModel.reset()
            return false
        }

        return when (navController.currentDestination?.id ?: 0) {
            R.id.eventCreateFragment -> showDismissEventDialog()
            R.id.commEditFragment -> resetCommViewModel()
            else -> false
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            model.query.value = intent.getStringExtra(SearchManager.QUERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommAddFragment.LOGIN_CREATE_COMM_REQUEST && resultCode == Activity.RESULT_OK) {
            model.commToCreate.value = data!!.getStringExtra(LoginActivity.COMMUNITY_NAME)
        }
    }

    override fun onSupportNavigateUp() = navController.navigateUp()
}
