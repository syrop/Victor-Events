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

package pl.org.seva.events.main.model.fs

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

abstract class FsBase {
    private val db = FirebaseFirestore.getInstance()

    protected val communities get() = db collection COMMUNITIES

    protected val String.admins get() = db collection PRIVATE document this collection COMM_ADMINS

    protected val String.events get() = db collection COMMUNITIES document this collection COMM_EVENTS

    protected val String.comm get() = db collection COMMUNITIES document this

    protected val String.privateComm get() = db collection PRIVATE document this

    protected val String.name get() = db collection COMMUNITIES document this collection COMM_NAME

    private infix fun CollectionReference.document(ch: String) = this.document(ch)

    private infix fun DocumentReference.collection(ch: String) = this.collection(ch)

    private infix fun FirebaseFirestore.collection(ref: String) = this.collection(ref)

    companion object {
        /** Root of community-related data that can be read by anyone. */
        const val COMMUNITIES = "communities"
        /** Root of user-related data that can be read only by logged in users. */
        const val PRIVATE = "private"
        /** Per community. */
        const val COMM_EVENTS = "events"
        /** Comm name. */
        const val COMM_NAME = "name"
        /** Comm description. */
        const val COMM_DESC = "desc"
        /** May not be null. */
        const val EVENT_NAME = "name"
        /** GeoPoint. */
        const val EVENT_LOCATION = "location"
        /** String representation of ZonedDateTime */
        const val EVENT_TIME = "time"
        /** Nullable. */
        const val EVENT_DESC = "description"

        /** Admin e-mails per community. */
        const val COMM_ADMINS = "admins"
        const val ADMIN_GRANTED = "admin_granted"
    }
}
