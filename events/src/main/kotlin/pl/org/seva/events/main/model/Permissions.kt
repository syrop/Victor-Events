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

@file:Suppress("EXPERIMENTAL_API_USAGE")

package pl.org.seva.events.main.model

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import pl.org.seva.events.main.extension.getViewModel
import pl.org.seva.events.main.init.instance

val permissions by instance<Permissions>()

class Permissions {

    fun onRequestPermissionsResult(
            fragment: Fragment,
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {

        val vm = fragment.getViewModel<ViewModel>()
        val granted = vm.granted
        val denied = vm.denied

        infix fun String.onGranted(requestCode: Int) =
                granted.offer(PermissionResult(requestCode, this))

        infix fun String.onDenied(requestCode: Int) =
                denied.offer(PermissionResult(requestCode, this))

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

    class ViewModel : androidx.lifecycle.ViewModel() {
        val granted by lazy { BroadcastChannel<PermissionResult>(Channel.CONFLATED) }
        val denied by lazy { BroadcastChannel<PermissionResult>(Channel.CONFLATED) }

        private fun CoroutineScope.watch(code: Int, request: PermissionRequest) = launch (Dispatchers.IO) {
            fun PermissionResult.matches() =
                    requestCode == code && permission == request.permission

            while (true) {
                select<Unit> {
                    granted.openSubscription().onReceive {
                        if (it.matches()) {
                            withContext(Dispatchers.Main) {
                                request.onGranted()
                            }
                        }
                    }
                    denied.openSubscription().onReceive {
                        if (it.matches()) {
                            withContext(Dispatchers.Main) {
                                request.onDenied()
                            }
                        }
                    }
                }
            }
        }

        fun watch(code: Int, requests: Array<PermissionRequest>) = viewModelScope.launch {
                for (req in requests) {
                    watch(code, req)
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
