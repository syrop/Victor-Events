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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import pl.org.seva.events.main.ui.InteractiveMapHolder
import pl.org.seva.events.main.ui.MapHolder
import pl.org.seva.events.main.data.Permissions
import kotlin.coroutines.resume

fun Fragment.nav(@IdRes resId: Int): Boolean {
    findNavController().navigate(resId)
    return true
}

fun Fragment.back(): Boolean {
    findNavController().popBackStack()
    return true
}

fun Fragment.createMapHolder(@IdRes map: Int, block: MapHolder.() -> Unit = {}) =
        MapHolder().apply(block).also {
            withMapHolder(it to map)
        }

fun Fragment.createInteractiveMapHolder(@IdRes map: Int, block: InteractiveMapHolder.() -> Unit = {}) =
        InteractiveMapHolder().apply(block).also {
            withMapHolder(it to map)
        }

private fun Fragment.withMapHolder(pair: Pair<MapHolder, Int>) {
    val (holder, id) = pair
    lifecycleScope.launch {
        holder withMap googleMap(id)
    }
}

fun Fragment.check(permission: String) =
        ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED

var Fragment.title: CharSequence
    get() = checkNotNull(checkNotNull((requireActivity()).actionBar).title)
    set(value) {
        checkNotNull((requireActivity() as AppCompatActivity).supportActionBar).title = value
    }

fun Fragment.inBrowser(uri: String) {
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(uri)
    startActivity(i)
}

fun Fragment.request(requestCode: Int, requests: Array<Permissions.PermissionRequest>) {
    watchPermissions(requestCode, requests)
    requestPermissions(requests.map { it.permission }.toTypedArray(), requestCode)
}

private fun Fragment.watchPermissions(requestCode: Int, requests: Array<Permissions.PermissionRequest>) {
    lifecycleScope.launch { viewModels<Permissions.ViewModel>().value.watch(requestCode, requests) }
}

fun Fragment.prefs(name: String): SharedPreferences =
        requireContext().getSharedPreferences(name, Context.MODE_PRIVATE)

suspend fun Fragment.googleMap(@IdRes id: Int) =
        suspendCancellableCoroutine<GoogleMap> { continuation ->
            val mapFragment =
                    childFragmentManager.findFragmentById(id) as SupportMapFragment
            mapFragment.getMapAsync { map -> continuation.resume(map) }
        }

fun Fragment.requestLocationPermission(onGranted: () -> Unit) {
    if (check(Manifest.permission.ACCESS_COARSE_LOCATION)) onGranted()
    else request(
            Permissions.DEFAULT_PERMISSION_REQUEST_ID,
            arrayOf(Permissions.PermissionRequest(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    onGranted = onGranted)))
}

fun Fragment.enableMyLocationOnResume(map: GoogleMap) {
    lifecycle.addObserver(object : LifecycleEventObserver{
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_RESUME && check(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                map.isMyLocationEnabled = true
            }
        }
    })
}

fun Fragment.onBack(block: () -> Unit) {

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = block()
    }
    requireActivity().onBackPressedDispatcher.addCallback(this, callback)
}

fun Fragment.question(message: String, yes: () -> Unit = {}, no: () -> Unit = {}) =
        requireContext().question(message, yes, no)

val Fragment.versionName get() = requireContext().versionName

fun Fragment.longToast(message: CharSequence) =
        toast(message, Toast.LENGTH_LONG)

fun Fragment.toast(@StringRes id: Int, duration: Int = Toast.LENGTH_SHORT) =
        toast(getString(id), duration)

fun Fragment.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().toast(message, duration)
}
