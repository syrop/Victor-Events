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

package pl.org.seva.events.login

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.log
import pl.org.seva.events.main.extension.toast
import kotlin.coroutines.resume

class Login {
    private val _isLoggedIn = MutableStateFlow(value = false)
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    var isLoggedIn: Boolean
        get() = _isLoggedIn.value
        set(value) {
            _isLoggedIn.value = value
        }

    var email: String = ""
    var displayName: String = ""

    fun setCurrentUser(user: FirebaseUser?) = if (user != null) {
        _isLoggedIn.value = true
        email = checkNotNull(user.email)
        displayName = checkNotNull(user.displayName)
    } else {
        _isLoggedIn.value = false
        email = ""
        displayName = ""
    }

    suspend fun signInWithGoogle(context: Context): Boolean {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(DEFAULT_WEB_CLIENT_ID)
                .build()
        val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

        return try {
            val result = credentialManager.getCredential(
                    request = request,
                    context = context,
            )
            val credential = result.credential
            if ((credential is CustomCredential) &&
                    (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                firebaseAuthWithGoogle(context, googleIdTokenCredential.idToken)
            } else {
                context.toast(R.string.login_authentication_failed)
                false
            }
        } catch (e: GetCredentialException) {
            log.warning("Credential Manager sign-in failed: $e")
            context.toast(R.string.login_authentication_failed)
            false
        }
    }

    private suspend fun firebaseAuthWithGoogle(context: Context, idToken: String): Boolean {
        val auth = FirebaseAuth.getInstance()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val success = suspendCancellableCoroutine { continuation ->
            auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        log.info("signInWithCredential:onComplete:${task.isSuccessful}")
                        if (continuation.isActive) {
                            continuation.resume(task.isSuccessful)
                        }
                    }
        }
        return if (success) {
            setCurrentUser(auth.currentUser)
            true
        } else {
            context.toast(R.string.login_authentication_failed)
            false
        }
    }

    suspend fun signOut(context: Context) {
        val credentialManager = CredentialManager.create(context)
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
            // Ignore credential clearing errors on sign-out
        }
        FirebaseAuth.getInstance().signOut()
        setCurrentUser(null)
    }

    companion object {
        const val DEFAULT_WEB_CLIENT_ID = "1052062614577-5hr7d9u1avkhcp615irenn79i13diskm.apps.googleusercontent.com"
    }
}
