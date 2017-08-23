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

package pl.org.seva.events.data.firebase

import android.util.Base64
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import pl.org.seva.events.data.Login

open class Fb : KodeinGlobalAware {

    protected val communities get() = db reference COMMUNITIES

    protected val login: Login = instance()

    protected val db = FirebaseDatabase.getInstance()!!

    protected val String.admins get() = db reference PRIVATE child this child ADMINS

    protected val String.events get() = db reference COMMUNITIES child this child EVENTS

    protected val String.reference get() = db reference COMMUNITIES child this

    protected infix fun DatabaseReference.child(ch: String): DatabaseReference = this.child(ch)

    protected infix fun DatabaseReference.value(v: Any) = this.setValue(v)!!

    protected infix fun FirebaseDatabase.reference(ref: String) : DatabaseReference = this.getReference(ref)

    protected fun String.to64() = Base64.encodeToString(toByteArray(), Base64.NO_WRAP)!!

    protected fun String.from64() = String(Base64.decode(toByteArray(), Base64.NO_WRAP))

    companion object {
        /** Root of community-related data that can be read by anyone. */
        val COMMUNITIES = "communities"
        /** Root of user-related data that can be read only by logged in users. */
        val PRIVATE = "private"
        /** Per community. */
        val EVENTS = "events"
        /** May not be null. */
        val EVENT_NAME = "name"
        /** Double. */
        val EVENT_LAT = "lat"
        /** Double. */
        val EVENT_LON = "lon"
        /** Start time in milliseconds (there is no duration nor end time). */
        val EVENT_TIME = "time"
        /** Nullable. */
        val EVENT_DESC = "description"
        /** Admin e-mails per community. */
        val ADMINS = "admins"
    }
}
