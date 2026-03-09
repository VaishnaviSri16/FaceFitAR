package eu.tutorials.facefitar.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import eu.tutorials.facefitar.facedetection.FaceDetectorProcessor
import androidx.camera.core.ExperimentalGetImage
import com.google.mlkit.vision.face.Face

class CameraManager {

    private val faceDetector = FaceDetectorProcessor()
    private var cameraProvider: ProcessCameraProvider? = null

    @OptIn(ExperimentalGetImage::class)
    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        isFrontCamera: Boolean,
        onFacesDetected: (List<Face>, Int, Int, Int) -> Unit
    ) {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({

            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val cameraSelector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalyzer.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->

                val mediaImage = imageProxy.image

                if (mediaImage != null) {

                    faceDetector.processImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    ) { faces, width, height, rotation ->

                        onFacesDetected(faces, width, height, rotation)

                        // CLOSE FRAME AFTER ML KIT FINISHES
                        imageProxy.close()
                    }

                } else {
                    imageProxy.close()
                }
            }

            try {

                cameraProvider?.unbindAll()

                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

            } catch (e: Exception) {

                Log.e("CameraManager", "Use case binding failed", e)
            }

        }, ContextCompat.getMainExecutor(context))
    }
}
