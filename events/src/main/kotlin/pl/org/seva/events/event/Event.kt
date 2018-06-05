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
 * If you like this program, consider donating bitcoin: 37vHXbpPcDBwcCTpZfjGL63JRwn6FPiXTS
 */

package pl.org.seva.events.event

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import java.time.ZonedDateTime

@SuppressLint("ParcelCreator")
@Parcelize
data class Event(
        val name: String = CREATION_NAME,
        val time: ZonedDateTime = ZonedDateTime.now(),
        val location: GeoPoint? = null,
        val desc: String? = null
) : Parcelable {

    val firestore get() = Firestore(name, time.toString(), location, desc)

    @Suppress("MemberVisibilityCanBePrivate")
    data class Firestore(
            val name: String,
            val time: String,
            val location: GeoPoint?,
            val desc: String?) {
        fun value() = Event(name, ZonedDateTime.parse(time), location, desc)
    }

    companion object: Parceler<Event> {
        private const val NOT_PRESENT = 0
        private const val PRESENT = 1

        private const val CREATION_NAME = ""

        val CREATION_EVENT = Event(CREATION_NAME)

        override fun Event.write(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            if (location != null) {
                parcel.writeInt(PRESENT)
                parcel.writeDouble(location.latitude)
                parcel.writeDouble(location.longitude)
            }
            else {
                parcel.writeInt(NOT_PRESENT)
            }
            parcel.writeString(time.toString())
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
            val location =
            if (parcel.readInt() == PRESENT) {
                val lat = parcel.readDouble()
                val lon = parcel.readDouble()
                GeoPoint(lat, lon)
            } else null
            val time = ZonedDateTime.parse(parcel.readString())

            val desc = if (parcel.readInt() == NOT_PRESENT) parcel.readString() else null
            return Event(name, time = time, location = location, desc = desc)
        }
    }
}
