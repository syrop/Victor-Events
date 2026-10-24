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

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.log
import pl.org.seva.events.main.extension.toast
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.EventsTheme

class LoginActivity : AppCompatActivity() {

    private val login by instance<Login>()

    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: (firebaseAuth: FirebaseAuth) -> Unit
    private val credentialManager by lazy { CredentialManager.create(this) }

    private var finishWhenReady: Boolean = false
    private var logoutWhenReady: Boolean = false

    private var commToCreate: String? = null

    private var inProgress by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        fun String.setResult() {
            setResult(RESULT_OK, Intent().putExtra(COMMUNITY_NAME, this))
        }

        fun onUserLoggedIn(user: FirebaseUser) {
            login.setCurrentUser(user)
            if (finishWhenReady) {
                commToCreate?.setResult() ?: setResult(RESULT_OK)
                finish()
            }
        }

        fun onUserLoggedOut() {
            login.setCurrentUser(null)
            if (finishWhenReady) {
                finish()
            }
        }

        fun signInFailed() {
            toast(R.string.login_authentication_failed)
            finish()
        }

        fun firebaseAuthWithGoogle(idToken: String) {
            inProgress = true
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) {
                        log.info("signInWithCredential:onComplete:" + it.isSuccessful)
                        inProgress = false

                        if (!it.isSuccessful) {
                            signInFailed()
                        }
                    }
        }

        fun login() {
            val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(DEFAULT_WEB_CLIENT_ID)
                    .build()
            val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(
                            request = request,
                            context = this@LoginActivity,
                    )
                    finishWhenReady = true
                    val credential = result.credential
                    if ((credential is CustomCredential) &&
                            (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
                    ) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                    } else {
                        signInFailed()
                    }
                } catch (_: GetCredentialException) {
                    signInFailed()
                }
            }
        }

        fun logout() {
            finishWhenReady = true
            logoutWhenReady = true
            lifecycleScope.launch {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (_: Exception) {
                    // Ignore credential clearing errors on sign-out
                }
                auth.signOut()
                login.setCurrentUser(null)
                setResult(RESULT_OK)
                finish()
            }
        }

        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        moveTaskToBack(true)
                        finish()
                    }
                },
        )
        setContent {
            EventsTheme {
                LoginScreen(inProgress = inProgress)
            }
        }
        commToCreate = intent.getStringExtra(COMMUNITY_NAME)
        auth = FirebaseAuth.getInstance()
        authStateListener = {
            val user = it.currentUser
            if (user != null) {
                log.info("onAuthStateChanged:signed_in:" + user.uid)
                onUserLoggedIn(user)
            } else {
                log.info("onAuthStateChanged:signed_out")
                onUserLoggedOut()
            }
        }

        when (intent.getStringExtra(ACTION)) {
            LOGOUT -> {
                logout()
            }
            LOGIN -> {
                login()
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    public override fun onStop() {
        super.onStop()
        inProgress = false
        auth.removeAuthStateListener(authStateListener)
    }

    companion object {

        const val DEFAULT_WEB_CLIENT_ID = "1052062614577-5hr7d9u1avkhcp615irenn79i13diskm.apps.googleusercontent.com"

        const val ACTION = "action"
        const val LOGIN = "login"
        const val LOGOUT = "log_out"

        const val COMMUNITY_NAME = "community_name"
    }
}

/**
 * Jetpack Compose screen replacing `res/layout/ac_login.xml`.
 */
@Composable
fun LoginScreen(inProgress: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (inProgress) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    EventsTheme {
        LoginScreen(inProgress = true)
    }
}
