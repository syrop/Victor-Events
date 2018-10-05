package pl.org.seva.events.event

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import pl.org.seva.events.R

class EventsActivity : AppCompatActivity() {

    override fun onSupportNavigateUp()
            = findNavController(R.id.nav_host_fragment).navigateUp()
}
