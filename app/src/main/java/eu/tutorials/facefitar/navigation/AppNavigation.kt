package eu.tutorials.facefitar.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.*
import eu.tutorials.facefitar.viewmodel.AuthViewModel
import eu.tutorials.facefitar.ui.login.LoginScreen
import eu.tutorials.facefitar.ui.signup.SignupScreen

@Composable
fun AppNavigation(
    viewModel: AuthViewModel,
    cameraScreen: @Composable (onLogout: () -> Unit) -> Unit
) {
    val navController = rememberNavController()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    // Handle navigation based on auth state changes
    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            navController.navigate("camera") {
                popUpTo(0) { inclusive = true }
            }
        }
        // Only navigate back to login if we're not currently on a screen that handles unauthenticated users
        // This prevents kicking the user from signup to login when they haven't logged in yet
        else if (navController.currentDestination?.route != "signup" && navController.currentDestination?.route != "login") {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) "camera" else "login"
    ) {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    // State change handles navigation
                },
                onSignupClick = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignupScreen(
                viewModel = viewModel,
                onSignupSuccess = {
                    // State change handles navigation
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("camera") {
            cameraScreen {
                // Logout callback
            }
        }
    }
}
