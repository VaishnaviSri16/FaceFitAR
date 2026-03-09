package eu.tutorials.facefitar.viewmodel

import androidx.lifecycle.ViewModel
import eu.tutorials.facefitar.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isUserLoggedIn = MutableStateFlow(authRepository.isUserLoggedIn())
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        _isLoading.value = true
        authRepository.login(email, password) { success, error ->
            _isLoading.value = false
            if (success) {
                _isUserLoggedIn.value = true
            }
            onResult(success, error)
        }
    }

    fun signup(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        _isLoading.value = true
        authRepository.signup(email, password) { success, error ->
            _isLoading.value = false
            if (success) {
                _isUserLoggedIn.value = true
            }
            onResult(success, error)
        }
    }

    fun logout() {
        authRepository.logout()
        _isUserLoggedIn.value = false
    }
}
