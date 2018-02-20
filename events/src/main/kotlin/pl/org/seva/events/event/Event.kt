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
 */

package pl.org.seva.events.event

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class Event(
        val name: String = CREATION_NAME,
        val time: Long = System.currentTimeMillis(),
        val lat: Double? = null,
        val lon: Double? = null,
        val desc: String? = null
) : Parcelable {

    companion object: Parceler<Event> {
        private const val NOT_PRESENT = 0
        private const val PRESENT = 1

        private const val CREATION_NAME = ""

        val creation get() = Event(CREATION_NAME, System.currentTimeMillis())

        override fun Event.write(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            if (lat != null && lon != null) {
                parcel.writeInt(PRESENT)
                parcel.writeDouble(lat)
                parcel.writeDouble(lon)
            }
            else {
                parcel.writeInt(NOT_PRESENT)
            }
            parcel.writeLong(time)
            if (desc != null) {
                parcel.writeInt(PRESENT)
                parcel.writeString(desc)
            }
            else {
                parcel.writeInt(NOT_PRESENT)
            }
        }

        override fun create(parcel: Parcel): Event {
            val name = parcel.readString()
            val containsLocation = parcel.readInt() != NOT_PRESENT
            val lat = if (containsLocation) parcel.readDouble() else null
            val lon = if (containsLocation) parcel.readDouble() else null
            val time = parcel.readLong()
            val containsDescription = parcel.readInt() != NOT_PRESENT
            val desc = if (containsDescription) parcel.readString() else null
            return Event(name, time = time, lat = lat, lon = lon, desc = desc)
        }
    }
}