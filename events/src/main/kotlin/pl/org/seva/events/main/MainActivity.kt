package pl.org.seva.events.main

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import pl.org.seva.events.R
import pl.org.seva.events.comm.CommAddFragment
import pl.org.seva.events.comm.CommAddViewModel
import pl.org.seva.events.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val commAddViewModel by viewModels<CommAddViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            commAddViewModel.query(intent.getStringExtra(SearchManager.QUERY)!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommAddFragment.LOGIN_CREATE_COMM_REQUEST && resultCode == Activity.RESULT_OK) {
            commAddViewModel.commToCreate.value = checkNotNull(data).getStringExtra(LoginActivity.COMMUNITY_NAME)
        }
    }

    override fun onSupportNavigateUp() = navController.navigateUp()
}
