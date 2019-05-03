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

package pl.org.seva.events.main.extension

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Job
import pl.org.seva.events.main.view.ui.InteractiveMapHolder
import pl.org.seva.events.main.view.ui.MapHolder
import pl.org.seva.events.main.model.Permissions

fun Fragment.nav(@IdRes resId: Int): Boolean {
    findNavController().navigate(resId)
    return true
}

fun Fragment.back() = view!!.post { findNavController().popBackStack() }

inline fun <reified R : ViewModel> Fragment.viewModel() = lazy { getViewModel<R>() }

inline fun <reified R : ViewModel> Fragment.getViewModel() = activity!!.getViewModel<R>()

fun Fragment.createMapHolder(@IdRes fragmentId: Int, block: MapHolder.() -> Unit = {}) =
        MapHolder().apply(block) withFragment (this + fragmentId)

fun Fragment.createInteractiveMapHolder(@IdRes fragmentId: Int, block: InteractiveMapHolder.() -> Unit = {}) =
        InteractiveMapHolder().apply(block) withFragment (this + fragmentId)

fun Fragment.hasPermission(permission: String) =
        ContextCompat.checkSelfPermission(context!!, permission) == PackageManager.PERMISSION_GRANTED

var Fragment.title: CharSequence get() = (activity!! as AppCompatActivity).supportActionBar!!.title!!
set(value) {
    (activity!! as AppCompatActivity).supportActionBar!!.title = value
}

fun Fragment.inBrowser(uri: String) {
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(uri)
    startActivity(i)
}

operator fun Fragment.plus(@IdRes id: Int) = this to id

fun Fragment.request(requestCode: Int, requests: Array<Permissions.PermissionRequest>) {
    watch(requestCode, requests)
    requestPermissions(requests.map { it.permission }.toTypedArray(), requestCode)
}

private fun Fragment.watch(requestCode: Int, requests: Array<Permissions.PermissionRequest>) {
    val vm = getViewModel<Permissions.ViewModel>()
    untilDestroy { vm.watch(requestCode, requests) }
}

private fun Fragment.untilDestroy(work: () -> Job) = work().apply {
    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                cancel()
            }
        }
    })
}
