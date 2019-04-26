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

package pl.org.seva.events.comm

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import pl.org.seva.events.R
import pl.org.seva.events.message.Message
import java.time.Duration
import java.time.LocalDateTime

class CommSyncWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override val coroutineContext = Dispatchers.IO

    override suspend fun doWork() = coroutineScope {
        val messages = comms.refresh()
                .filter { it.isDummy }
                .map { Message(
                        LocalDateTime.now(),
                        context.getString(R.string.system_message_comm_deleted)
                                .replace(NAME_PLACEHOLDER, it.name)) }
        Result.success()
    }

    companion object {
        val TAG: String = this::class.java.name
        val FREQUENCY: Duration = Duration.ofHours(2)
        const val NAME_PLACEHOLDER = "[name]"
    }
}
