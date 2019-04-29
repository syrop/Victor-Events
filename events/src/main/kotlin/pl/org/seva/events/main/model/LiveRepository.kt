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

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.org.seva.events.main.model.livedata.DefaultHotData
import pl.org.seva.events.main.model.livedata.HotData
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.launch

abstract class LiveRepository {

    private val liveData = MutableLiveData<Unit>()

    private val channel = BroadcastChannel<Unit>(Channel.CONFLATED)

    protected fun notifyDataSetChanged() {
        if (Looper.getMainLooper().thread === Thread.currentThread()) liveData.value = Unit
        else liveData.postValue(Unit)
        channel.sendBlocking(Unit)
    }

    infix fun vm(vm: ViewModel) = { block: () -> Unit ->
        vm.viewModelScope.launch { this@LiveRepository(block) }
    }

    operator fun plus(owner: LifecycleOwner): HotData<Unit> = DefaultHotData(liveData, owner)

    suspend operator fun invoke(block: () -> Unit) {
        with (channel.openSubscription()) {
            if (!isEmpty) receive()
            while(true) {
                receive()
                block()
            }
        }
    }
}
