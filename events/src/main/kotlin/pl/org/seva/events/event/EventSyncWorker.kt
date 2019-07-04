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

package pl.org.seva.events.event

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestoreException
import pl.org.seva.events.main.data.SyncWorker
import pl.org.seva.events.main.init.instance
import java.time.Duration

class EventSyncWorker(context: Context, params: WorkerParameters) :
        CoroutineWorker(context, params), SyncWorker {

    private val events by instance<Events>()

    override suspend fun doWork() = syncCoroutineScope {
        try {
            events.refresh()
        }
        catch (e: FirebaseFirestoreException) {}
        Result.success()
    }

    companion object {
        val TAG: String = this::class.java.name
        val FREQUENCY: Duration = Duration.ofHours(3)
        val POLICY = ExistingPeriodicWorkPolicy.REPLACE
    }
}
