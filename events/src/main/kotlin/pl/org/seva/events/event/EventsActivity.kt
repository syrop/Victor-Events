package pl.org.seva.events.event

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_events.*
import pl.org.seva.events.R
import pl.org.seva.events.comm.AddCommFragment
import pl.org.seva.events.comm.communities
import pl.org.seva.events.login.LoginActivity
import pl.org.seva.events.tools.EventsViewModel

class EventsActivity : AppCompatActivity() {

    private val nav get() = findNavController(R.id.nav_host_fragment)

    private lateinit var eventsModel: EventsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)
        setSupportActionBar(toolbar)
        NavigationUI.setupActionBarWithNavController(this, nav)
        eventsModel = ViewModelProviders.of(this).get(EventsViewModel::class.java)
        if (communities.isEmpty) {
            nav.navigate(R.id.action_eventsFragment_to_addCommFragment)
        }
        nav.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.eventsFragment && communities.isEmpty) {
                finish()
            }
            else if (destination.id == R.id.addCommFragment && communities.isEmpty) {
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                supportActionBar!!.setDisplayShowHomeEnabled(false)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            eventsModel.query.value = intent.getStringExtra(SearchManager.QUERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddCommFragment.LOGIN_CREATE_COMM_REQUEST && resultCode == Activity.RESULT_OK) {
            eventsModel.commToCreate.value = data!!.getStringExtra(LoginActivity.COMMUNITY_NAME)
        }
    }

    override fun onSupportNavigateUp() = nav.navigateUp()
}
