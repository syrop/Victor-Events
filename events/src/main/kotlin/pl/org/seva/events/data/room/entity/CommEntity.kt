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

package pl.org.seva.events.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.graphics.Color
import pl.org.seva.events.comm.Community
import pl.org.seva.events.data.room.EventsDb

@Entity(tableName = EventsDb.COMMUNITIES_TABLE_NAME)
class CommEntity() {
    @PrimaryKey
    lateinit var name: String
    var color: Int = Color.GRAY
    var admin: Boolean = false

    constructor(community: Community): this() {
        name = community.name
        color = community.color
        admin = community.admin
    }

    fun comValue() = Community(name = name, color = color, admin = admin)
}
