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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_login_confirmation.*
import pl.org.seva.events.R
import pl.org.seva.events.comm.comms
import pl.org.seva.events.main.extension.boldSection
import pl.org.seva.events.main.extension.popBackStack

class LoginConfirmationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fun onContainerClicked() {
            startActivityForResult(Intent(activity, LoginActivity::class.java)
                    .putExtra(LoginActivity.ACTION, LoginActivity.LOGIN), LOGIN_CREATE_COMM_REQUEST)
        }

        super.onViewCreated(view, savedInstanceState)
        prompt.text = getString(R.string.login_confirmation_prompt).boldSection(
                TAP_ANYWHERE_PLACEHOLDER,
                getString(R.string.login_confirmation_tap_anywhere))

        layout.setOnClickListener { onContainerClicked() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_CREATE_COMM_REQUEST && resultCode == Activity.RESULT_OK) {
            prompt.visibility = View.GONE
            progress.visibility = View.VISIBLE
            comms.refreshAdminStatus().observe(this, Observer { popBackStack() })
        }
    }

    companion object {
        const val LOGIN_CREATE_COMM_REQUEST = 0
        @Suppress("SpellCheckingInspection")
        const val TAP_ANYWHERE_PLACEHOLDER = "[TAPANYWHERE]"
    }

}
