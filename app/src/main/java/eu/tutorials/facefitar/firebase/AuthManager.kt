package eu.tutorials.facefitar.firebase

import com.google.firebase.auth.FirebaseAuth

class AuthManager {

    private val auth = FirebaseAuth.getInstance()

    fun login(
        email: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                onResult(it.isSuccessful)
            }

    }

    fun signup(
        email: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                onResult(it.isSuccessful)
            }

    }
}