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

package pl.org.seva.events.comm

import android.graphics.Color
import androidx.room.PrimaryKey
import pl.org.seva.events.main.data.db.EventsDb
import pl.org.seva.events.main.init.instance
import java.util.*

private val defaultComms by instance<Comms>()

data class Comm(
        val name: String,
        val desc: String = "",
        val color: Int = Color.GRAY,
        val isAdmin: Boolean = false,
        val comms: Comms = defaultComms,
) {

    private val attributes = mutableMapOf<String, String>()

    var originalName get() = if (isDummy) checkNotNull(attributes[ORIG_NAME]) else name
    set(value)  { if (isDummy) attributes[ORIG_NAME] = value }

    val lcName: String get() = name.toLowerCase(Locale.getDefault())

    val isDummy get() = name == DUMMY_NAME

    val isMemberOf get () = this in comms

    suspend fun leave() = comms leave this

    suspend fun delete() = comms delete this

    suspend fun join() = comms join this

    fun update() = comms update  this

    @androidx.room.Entity(tableName = EventsDb.COMM_TABLE)
    class Entity() {
        @PrimaryKey
        lateinit var name: String
        lateinit var description: String
        var color: Int = Color.GRAY
        var isAdmin: Boolean = false

        constructor(comm: Comm) : this() {
            name = comm.name
            description = comm.desc
            color = comm.color
            isAdmin = comm.isAdmin
        }

        fun value() = Comm(name = name, desc = description, color = color, isAdmin = isAdmin)
    }

    companion object {
        const val DUMMY_NAME = ""
        private const val ORIG_NAME = "original_name"
        private const val DUMMY_DESCRIPTION = ""
        val DUMMY by lazy { Comm(DUMMY_NAME, DUMMY_DESCRIPTION) }
    }
}
