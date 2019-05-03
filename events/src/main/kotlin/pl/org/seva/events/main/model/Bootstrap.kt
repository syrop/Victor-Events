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

package pl.org.seva.events.main.model

import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import pl.org.seva.events.comm.CommSyncWorker
import pl.org.seva.events.comm.comms
import pl.org.seva.events.event.EventSyncWorker
import pl.org.seva.events.event.events
import pl.org.seva.events.login.login
import pl.org.seva.events.message.messages
import java.time.Duration

val bootstrap by instance<Bootstrap>()

class Bootstrap {

    private inline fun <reified W : ListenableWorker> scheduleSync(
            tag: String,
            frequency: Duration,
            policy: ExistingPeriodicWorkPolicy) {
        WorkManager.getInstance().enqueueUniquePeriodicWork(
                tag,
                policy,
                PeriodicWorkRequestBuilder<W>(frequency)
                        .setConstraints(Constraints.Builder()
                                .setRequiresBatteryNotLow(true)
                                .setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .build())
    }

    suspend fun boot() = coroutineScope {
        login.setCurrentUser(FirebaseAuth.getInstance().currentUser)
        listOf(
            launch { comms.fromDb() },
            launch { messages.fromDb() },
            launch { events.fromDb() })
                .joinAll()
        scheduleSync<CommSyncWorker>(
                tag = CommSyncWorker.TAG,
                policy = CommSyncWorker.POLICY,
                frequency = CommSyncWorker.FREQUENCY)
        scheduleSync<EventSyncWorker>(
                tag = EventSyncWorker.TAG,
                policy = EventSyncWorker.POLICY,
                frequency = EventSyncWorker.FREQUENCY)
    }
}
