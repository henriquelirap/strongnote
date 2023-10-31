package br.com.utfpr.edu.strongnote.util

import br.com.utfpr.edu.strongnote.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database

class FirebaseHelper {

    companion object {
        fun getDatabase() = Firebase.database.reference
        fun getAuth() = FirebaseAuth.getInstance()
        fun getIdUser() = getAuth().currentUser?.uid ?: ""
        fun isAutenticated() = getAuth().currentUser != null
        fun validError(error: String): Int {
            return when {
                error.contains("There is no user record corresponding to this identifier") -> {
                    R.string.account_not_exists
                }

                error.contains("The email address is badly formatted") -> {
                    R.string.email_invalid
                }

                error.contains("The password is invalid or the user does not have a password") -> {
                    R.string.password_incorret
                }

                error.contains("The email address is already in use by another account") -> {
                    R.string.email_in_use
                }

                error.contains("Password should be at least 6 characters") -> {
                    R.string.password_weak
                }

                error.contains("INVALID_LOGIN_CREDENTIALS") -> {
                    R.string.invalid_credentials
                }

                else -> {
                    R.string.error
                }
            }
        }
    }
}