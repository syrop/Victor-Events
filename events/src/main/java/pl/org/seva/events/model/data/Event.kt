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

package pl.org.seva.events.model.data

import android.os.Parcel
import android.os.Parcelable

class Event(val name: String, val lat: Double, val lon: Double, val time: Long, val desc: String? = null):
        Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeDouble(lat)
        parcel.writeDouble(lon)
        parcel.writeLong(time)
        parcel.writeString(desc)
    }

    constructor(parcel: Parcel): this(parcel.readString(), parcel.readDouble(), parcel.readDouble(),
            parcel.readLong(), parcel.readString())

    override fun describeContents() = 0

    companion object {
        @Suppress("unused")
        @JvmField val CREATOR = object : Parcelable.Creator<Event> {
            override fun createFromParcel(parcel: Parcel) = Event(parcel)
            override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
        }
    }
}
