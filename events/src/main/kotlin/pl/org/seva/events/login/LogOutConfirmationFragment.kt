/*
 * Copyright (C) 2019 Wiktor Nizio
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

package pl.org.seva.events.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pl.org.seva.events.R
import pl.org.seva.events.comm.Comms
import pl.org.seva.events.main.extension.back
import pl.org.seva.events.main.extension.invoke
import pl.org.seva.events.main.init.instance

class LogOutConfirmationFragment : Fragment(R.layout.fr_log_out_conf) {

    private val comms by instance<Comms>()

    private val logOut by lazy { requireActivity().findViewById<View>(R.id.log_out) }
    private val cancel by lazy { requireActivity().findViewById<View>(R.id.cancel) }
    private val prompt by lazy { requireActivity().findViewById<View>(R.id.prompt) }
    private val progress by lazy { requireActivity().findViewById<View>(R.id.progress) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        logOut {
            val intent = Intent(activity, LoginActivity::class.java)
                    .putExtra(LoginActivity.ACTION, LoginActivity.LOGOUT)
            startActivityForResult(intent, LOG_OUT_REQUEST)
        }
        cancel {
            back()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOG_OUT_REQUEST && resultCode == Activity.RESULT_OK) {
            prompt.visibility = View.GONE
            logOut.visibility = View.GONE
            cancel.visibility = View.GONE
            progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                comms.refreshAdminStatuses()
                back()
            }
        }
    }

    companion object {
        const val LOG_OUT_REQUEST = 0
    }
}
