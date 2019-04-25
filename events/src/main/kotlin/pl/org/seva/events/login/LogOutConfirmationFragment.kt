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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fr_log_out_conf.*
import pl.org.seva.events.R
import pl.org.seva.events.comm.comms
import pl.org.seva.events.main.extension.back
import pl.org.seva.events.main.extension.inflate
import pl.org.seva.events.main.extension.invoke

class LogOutConfirmationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflate(R.layout.fr_log_out_conf, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        log_out {
            val intent = Intent(activity, LoginActivity::class.java)
                    .putExtra(LoginActivity.ACTION, LoginActivity.LOGOUT)
            startActivityForResult(intent, LOG_OUT_REQUEST)
        }

        cancel { back() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOG_OUT_REQUEST && resultCode == Activity.RESULT_OK) {
            prompt.visibility = View.GONE
            log_out.visibility = View.GONE
            cancel.visibility = View.GONE
            progress.visibility = View.VISIBLE
            comms.refreshAdminStatuses()
            back()
        }
    }

    companion object {
        const val LOG_OUT_REQUEST = 0
    }
}
