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

package pl.org.seva.events.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity
data class Event(val name: String, val lat: Double?, val lon: Double?, @PrimaryKey val time: Long,
                 val desc: String? = null): Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        if (lat != null && lon != null) {
            parcel.writeInt(1)
            parcel.writeDouble(lat)
            parcel.writeDouble(lon)
        }
        else {
            parcel.writeInt(0)
        }
        parcel.writeLong(time)
        if (desc != null) {
            parcel.writeInt(1)
            parcel.writeString(desc)
        }
        else {
            parcel.writeInt(0)
        }
    }

    override fun describeContents() = 0

    companion object {
        @Suppress("unused")
        @JvmField val CREATOR = object : Parcelable.Creator<Event> {
            override fun createFromParcel(parcel: Parcel): Event {
                val name = parcel.readString()
                val containsLatLng = parcel.readInt() != 0
                val lat = if (containsLatLng) parcel.readDouble() else null
                val lon = if (containsLatLng) parcel.readDouble() else null
                val time = parcel.readLong()
                val containsDescription = parcel.readInt() != 0
                val desc = if (containsDescription) parcel.readString() else null
                return Event(name, lat, lon, time, desc)
            }
            override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
        }
    }
}
