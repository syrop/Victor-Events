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

package pl.org.seva.events.main.data

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.channels.trySendBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class LiveRepository {

    private val broadcastChannel = BroadcastChannel<Unit>(Channel.CONFLATED)

    protected fun notifyDataSetChanged() {
        broadcastChannel.trySendBlocking(Unit)
    }

    fun updatedLiveData(scope: CoroutineScope) = updatedLiveData(scope.coroutineContext)

    fun updatedLiveData(context: CoroutineContext = EmptyCoroutineContext) = liveData(context) {
        with (broadcastChannel.openSubscription()) {
            try {
                while (true) {
                    emit(receive())
                }
            }
            finally {
                cancel()
            }
        }
    }
}
