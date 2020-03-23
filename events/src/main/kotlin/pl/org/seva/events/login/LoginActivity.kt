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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.ac_login.*
import pl.org.seva.events.R
import pl.org.seva.events.main.extension.log
import pl.org.seva.events.main.extension.toast
import pl.org.seva.events.main.init.instance

class LoginActivity : AppCompatActivity() {

    private val login by instance<Login>()

    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: (firebaseAuth: FirebaseAuth) -> Unit

    private var finishWhenReady: Boolean = false
    private var logoutWhenReady: Boolean = false

    private var commToCreate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        fun String.setResult() {
            setResult(Activity.RESULT_OK, Intent().putExtra(COMMUNITY_NAME, this))
        }

        fun onUserLoggedIn(user: FirebaseUser) {
            login.setCurrentUser(user)
            if (finishWhenReady) {
                commToCreate?.setResult() ?: setResult(Activity.RESULT_OK)
                finish()
            }
        }

        fun onUserLoggedOut() {
            login.setCurrentUser(null)
            if (finishWhenReady) {
                finish()
            }
        }

        fun login() {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(DEFAULT_WEB_CLIENT_ID)
                    .requestEmail()
                    .build()
            val client = GoogleSignIn.getClient(this, gso)
            val signInIntent = client.signInIntent
            startActivityForResult(signInIntent, SIGN_IN_REQUEST_ID)
        }

        fun logout() {
            finishWhenReady = true
            logoutWhenReady = true
            auth.signOut()
            login.setCurrentUser(null)
            setResult(Activity.RESULT_OK)
            finish()
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_login)
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
        progress.visibility = View.GONE
        auth.removeAuthStateListener(authStateListener)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
        finish()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        fun signInFailed() {
            toast(R.string.login_authentication_failed)
            finish()
        }

        fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
            log.info("firebaseAuthWithGoogle:" + checkNotNull(acct.id))
            progress.visibility = View.VISIBLE

            val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
            auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) {
                        log.info("signInWithCredential:onComplete:" + it.isSuccessful)
                        progress.visibility = View.GONE

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!it.isSuccessful) {
                            signInFailed()
                        }
                    }
        }

        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == SIGN_IN_REQUEST_ID) {
            finishWhenReady = true
            try {
                val account = checkNotNull(GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException::class.java))
                firebaseAuthWithGoogle(account)
            }
            catch (e: ApiException) {
                signInFailed()
            }
        }
    }

    companion object {

        const val DEFAULT_WEB_CLIENT_ID = "1052062614577-5hr7d9u1avkhcp615irenn79i13diskm.apps.googleusercontent.com"

        const val ACTION = "action"
        const val LOGIN = "login"
        const val LOGOUT = "log_out"

        const val COMMUNITY_NAME = "community_name"

        private const val SIGN_IN_REQUEST_ID = 9001
    }
}
