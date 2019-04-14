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

package pl.org.seva.events.main.permission

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import io.reactivex.subjects.PublishSubject
import pl.org.seva.events.main.extension.observe
import pl.org.seva.events.main.init.instance

val permissions by instance<Permissions>()

class Permissions {

    private val grantedSubject = PublishSubject.create<PermissionResult>()
    private val deniedSubject = PublishSubject.create<PermissionResult>()

    fun request(
            fragment: Fragment,
            requestCode: Int,
            requests: Array<PermissionRequest>) {
        val permissionsToRequest = ArrayList<String>()
        requests.forEach { permission ->
            permissionsToRequest.add(permission.permission)
            grantedSubject
                    .filter { it.requestCode == requestCode && it.permission == permission.permission }
                    .observe(fragment) { permission.onGranted() }
            deniedSubject
                    .filter { it.requestCode == requestCode && it.permission == permission.permission }
                    .observe(fragment) { permission.onDenied() }
        }
        fragment.requestPermissions(permissionsToRequest.toTypedArray(), requestCode)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        infix fun String.onGranted(requestCode: Int) =
                grantedSubject.onNext(PermissionResult(requestCode, this))

        infix fun String.onDenied(requestCode: Int) =
                deniedSubject.onNext(PermissionResult(requestCode, this))

        if (grantResults.isEmpty()) {
            permissions.forEach { it onDenied requestCode }
        } else repeat(permissions.size) { id ->
            if (grantResults[id] == PackageManager.PERMISSION_GRANTED) {
                permissions[id] onGranted requestCode
            } else {
                permissions[id] onDenied requestCode
            }
        }
    }

    companion object {
        const val DEFAULT_PERMISSION_REQUEST_ID = 0
    }

    data class PermissionResult(val requestCode: Int, val permission: String)

    class PermissionRequest(
            val permission: String,
            val onGranted: () -> Unit = {},
            val onDenied: () -> Unit = {})
}
