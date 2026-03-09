package eu.tutorials.facefitar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.tutorials.facefitar.viewmodel.AuthViewModel
import eu.tutorials.facefitar.camera.CameraManager
import eu.tutorials.facefitar.navigation.AppNavigation
import eu.tutorials.facefitar.ui.camera.CameraScreen
import eu.tutorials.facefitar.viewmodel.FaceFilterViewModel

class MainActivity : ComponentActivity() {

    private val cameraManager = CameraManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestCameraPermission()

        setContent {
            val authViewModel = viewModel<AuthViewModel>()
            val faceFilterViewModel = viewModel<FaceFilterViewModel>()

            AppNavigation(
                viewModel = authViewModel,
                cameraScreen = { onLogout ->
                    CameraScreen(
                        cameraManager = cameraManager,
                        faceFilterViewModel = faceFilterViewModel,
                        authViewModel = authViewModel,
                        onLogout = onLogout
                    )
                }
            )
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        }
    }
}
