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
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import pl.org.seva.events.main.model.SyncWorker
import java.time.Duration

class EventSyncWorker(context: Context, params: WorkerParameters) :
        CoroutineWorker(context, params), SyncWorker {
    override val coroutineContext = Dispatchers.IO

    override suspend fun doWork() = syncCoroutineScope {
        events.refresh()
        Result.success()
    }

    companion object {
        val TAG: String = this::class.java.name
        val FREQUENCY: Duration = Duration.ofHours(2)
    }
}
