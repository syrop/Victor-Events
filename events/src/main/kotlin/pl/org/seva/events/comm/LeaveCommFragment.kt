package pl.org.seva.events.comm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_create_event.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.withObjects

class LeaveCommFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_leave_comm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fun onCommunitySelected(comm: Comm) {

        }

        comms.names.apply {
            comm_layout.visibility = View.VISIBLE
            comm_spinner.withObjects(context!!, this) {
                position -> onCommunitySelected(comms[position])
            }
        }
    }
}
