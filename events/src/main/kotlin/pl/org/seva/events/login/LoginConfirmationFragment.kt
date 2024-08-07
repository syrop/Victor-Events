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
import pl.org.seva.events.main.extension.*
import pl.org.seva.events.main.init.instance

class LoginConfirmationFragment : Fragment(R.layout.fr_login_conf) {

    private val comms by instance<Comms>()

    private val ok = requireActivity().findViewById<View>(R.id.ok)
    private val cancel = requireActivity().findViewById<View>(R.id.cancel)
    private val privacyPolicy = requireActivity().findViewById<View>(R.id.privacy_policy)
    private val progress = requireActivity().findViewById<View>(R.id.progress)
    private val prompt = requireActivity().findViewById<View>(R.id.prompt)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        ok {
            val intent = Intent(activity, LoginActivity::class.java)
                    .putExtra(LoginActivity.ACTION, LoginActivity.LOGIN)
            startActivityForResult(intent, LOGIN_REQUEST)
        }
        cancel {
            back()
        }
        privacyPolicy {
            inBrowser(getString(R.string.login_confirmation_privacy_uri))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST && resultCode == Activity.RESULT_OK) {
            prompt.visibility = View.GONE
            ok.visibility = View.GONE
            privacyPolicy.visibility = View.GONE
            cancel.visibility = View.GONE
            progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                comms.refreshAdminStatuses()
                back()
            }
        }
    }

    companion object {
        const val LOGIN_REQUEST = 0
    }
}
