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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

open class Fb {

    protected val communities get() = db reference COMMUNITIES

    protected val login = pl.org.seva.events.login.login()

    private val db = FirebaseFirestore.getInstance()

    protected val String.admins get() = db reference PRIVATE child this child COMM_ADMINS

    protected val String.events get() = db reference COMMUNITIES child this child EVENTS

    protected val String.name get() = db reference COMMUNITIES child this child NAME

    protected infix fun CollectionReference.child(ch: String) = this.document(ch)

    protected infix fun DocumentReference.child(ch: String) = this.collection(ch)

    private infix fun FirebaseFirestore.reference(ref: String) = this.collection(ref)

    protected fun String.to64() = Base64.encodeToString(toByteArray(), Base64.NO_WRAP)!!

    protected fun String.from64() = String(Base64.decode(toByteArray(), Base64.NO_WRAP))

    companion object {
        /** Root of community-related data that can be read by anyone. */
        const val COMMUNITIES = "communities"
        /** Root of user-related data that can be read only by logged in users. */
        const val PRIVATE = "private"
        /** Per community. */
        const val EVENTS = "events"
        /** Community name. */
        const val NAME = "name"
        /** May not be null. */
        const val EVENT_NAME = "name"
        /** Double. */
        const val EVENT_LAT = "lat"
        /** Double. */
        const val EVENT_LON = "lon"
        /** Start time in milliseconds (there is no duration nor end time). */
        const val EVENT_TIME = "time"
        /** Nullable. */
        const val EVENT_DESC = "description"
        /** May not be null. */
        const val COMM_NAME = "name"

        /** Admin e-mails per community. */
        const val COMM_ADMINS = "admins"
    }
}
