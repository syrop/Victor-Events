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

package pl.org.seva.events.main.init

import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import pl.org.seva.events.comm.CommSyncWorker
import pl.org.seva.events.comm.comms
import pl.org.seva.events.comm.getAllValues
import pl.org.seva.events.event.EventSyncWorker
import pl.org.seva.events.login.login
import pl.org.seva.events.main.model.io
import pl.org.seva.events.main.model.db.db
import pl.org.seva.events.message.getAllValues
import pl.org.seva.events.message.messages
import java.time.Duration

val bootstrap by instance<Bootstrap>()

class Bootstrap {

    private inline fun <reified W : ListenableWorker> scheduleSync(tag: String, frequency: Duration) {
        WorkManager.getInstance().enqueueUniquePeriodicWork(
                tag,
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<W>(frequency)
                        .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                        .build())
    }

    fun boot() {
        login.setCurrentUser(FirebaseAuth.getInstance().currentUser)
        io {
            listOf(
                launch { comms cache db.commDao.getAllValues() },
                launch { messages add db.messageDao.getAllValues() })
                    .joinAll()
            scheduleSync<CommSyncWorker>(CommSyncWorker.TAG, CommSyncWorker.FREQUENCY)
            scheduleSync<EventSyncWorker>(EventSyncWorker.TAG, EventSyncWorker.FREQUENCY)
        }
    }

    fun login(user: FirebaseUser) {
        login.setCurrentUser(user)
    }

    fun logout() {
        login.setCurrentUser(null)
    }
}
