package pl.org.seva.events.event

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_events.*
import pl.org.seva.events.R
import pl.org.seva.events.comm.AddCommFragment
import pl.org.seva.events.comm.comms
import pl.org.seva.events.main.extension.viewModel
import pl.org.seva.events.login.LoginActivity
import pl.org.seva.events.main.EventsViewModel
import pl.org.seva.events.main.extension.question

class EventsActivity : AppCompatActivity() {

    private val nav by lazy { findNavController(R.id.nav_host_fragment) }

    private val model by viewModel<EventsViewModel>()

    private val createEventsViewModel by viewModel<CreateEventViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)
        setSupportActionBar(toolbar)
        NavigationUI.setupActionBarWithNavController(this, nav)

        nav.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.eventsFragment && comms.isEmpty) {
                finish()
            }
            else if (destination.id == R.id.addCommFragment && comms.isEmpty) {
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                supportActionBar!!.setDisplayShowHomeEnabled(false)
            }
        }
    }

    private fun showDismissEventDialog(): Boolean {
        return if (nav.currentDestination?.id == R.id.createEventFragment && createEventsViewModel.isFilledIn) {
            question(
                    message = getString(R.string.events_activity_dismiss_event),
                    yes = {
                        createEventsViewModel.clear()
                        nav.popBackStack()
                    })
            true
        }
        else false
    }

    override fun onBackPressed() {
        if (!showDismissEventDialog()) {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) showDismissEventDialog() else false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            model.query.value = intent.getStringExtra(SearchManager.QUERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddCommFragment.LOGIN_CREATE_COMM_REQUEST && resultCode == Activity.RESULT_OK) {
            model.commToCreate.value = data!!.getStringExtra(LoginActivity.COMMUNITY_NAME)
        }
    }

    override fun onSupportNavigateUp() = nav.navigateUp()
}
