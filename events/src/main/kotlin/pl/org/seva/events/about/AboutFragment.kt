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

package pl.org.seva.events.about

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fr_about.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.inBrowser
import pl.org.seva.events.main.extension.invoke
import pl.org.seva.events.main.extension.versionName

class AboutFragment : Fragment(R.layout.fr_about) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        version.text = getString(R.string.about_version).replace(VERSION_PLACEHOLDER, versionName)

        github { inBrowser(getString(R.string.about_github_url)) }
    }

    companion object {
        const val VERSION_PLACEHOLDER = "[ver]"
    }
}
