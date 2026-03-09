package eu.tutorials.facefitar.facedetection

import android.media.Image
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*

class FaceDetectorProcessor {
    private val detector: FaceDetector

    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()
        detector = FaceDetection.getClient(options)
    }

    fun processImage(
        image: Image,
        rotation: Int,
        onResult: (List<Face>, Int, Int, Int) -> Unit
    ) {
        val inputImage = InputImage.fromMediaImage(image, rotation)
        // ML Kit's inputImage automatically swaps width/height based on rotation
        val width = inputImage.width
        val height = inputImage.height

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    Log.d("FaceDetector", "Detected ${faces.size} faces at $width x $height")
                }
                onResult(faces, width, height, rotation)
            }
            .addOnFailureListener { e ->
                Log.e("FaceDetector", "Detection failed", e)
                onResult(emptyList(), width, height, rotation)
            }
    }
}
