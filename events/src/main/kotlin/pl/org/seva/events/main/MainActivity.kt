package pl.org.seva.events.main

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.ac_main.*
import pl.org.seva.events.R
import pl.org.seva.events.comm.CommAddFragment
import pl.org.seva.events.comm.CommViewModel
import pl.org.seva.events.event.EventCreateViewModel
import pl.org.seva.events.main.extension.viewModel
import pl.org.seva.events.login.LoginActivity
import pl.org.seva.events.main.extension.question
import pl.org.seva.events.main.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    private val mainModel by viewModel<MainViewModel>()

    private val createEventsViewModel by viewModel<EventCreateViewModel>()

    private val commViewModel by viewModel<CommViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_main)
        setSupportActionBar(toolbar)
        NavigationUI.setupActionBarWithNavController(this, navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (mainModel.pastDestination) {
                R.id.addCommFragment -> mainModel.resetQuery()
            }
            mainModel.pastDestination = destination.id
        }
    }

    override fun onBackPressed() {
        if (!onBackOrHomePressed()) {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> onBackOrHomePressed()
        else -> false
    } || super.onOptionsItemSelected(item)

    private fun onBackOrHomePressed(): Boolean {
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
            mainModel.query(intent.getStringExtra(SearchManager.QUERY))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommAddFragment.LOGIN_CREATE_COMM_REQUEST && resultCode == Activity.RESULT_OK) {
            mainModel.commToCreate.value = data!!.getStringExtra(LoginActivity.COMMUNITY_NAME)
        }
    }

    override fun onSupportNavigateUp() = navController.navigateUp()
}
