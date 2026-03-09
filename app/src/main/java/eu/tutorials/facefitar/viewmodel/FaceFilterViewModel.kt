package eu.tutorials.facefitar.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.face.Face
import eu.tutorials.facefitar.data.repository.CaptureRecord
import eu.tutorials.facefitar.data.repository.HistoryRepository
import eu.tutorials.facefitar.filters.FilterModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class FaceFilterViewModel : ViewModel() {

    private val historyRepository = HistoryRepository()

    private val _detectedFaces = MutableStateFlow<List<Face>>(emptyList())
    val detectedFaces: StateFlow<List<Face>> = _detectedFaces

    private val _selectedFilter = MutableStateFlow<FilterModel?>(null)
    val selectedFilter: StateFlow<FilterModel?> = _selectedFilter

    private val _lastCapturedUri = MutableStateFlow<Uri?>(null)
    val lastCapturedUri: StateFlow<Uri?> = _lastCapturedUri

    private val _capturedImages = MutableStateFlow<List<Uri>>(emptyList())
    val capturedImages: StateFlow<List<Uri>> = _capturedImages

    private val _captureHistory = MutableStateFlow<List<CaptureRecord>>(emptyList())
    val captureHistory: StateFlow<List<CaptureRecord>> = _captureHistory

    init {
        // Start listening to real-time Firestore updates immediately
        viewModelScope.launch {
            historyRepository.getCaptureHistoryFlow().collectLatest { history ->
                _captureHistory.value = history
            }
        }
    }

    fun selectFilter(filter: FilterModel) {
        viewModelScope.launch {
            _selectedFilter.value = filter
        }
    }

    fun onFacesDetected(faces: List<Face>) {
        viewModelScope.launch {
            _detectedFaces.value = faces
        }
    }

    fun setLastCapturedUri(uri: Uri?, filterName: String) {
        viewModelScope.launch {
            _lastCapturedUri.value = uri
            uri?.let {
                _capturedImages.value = _capturedImages.value + it
                // Log to Firestore. The real-time listener will handle the UI update.
                historyRepository.logCapture(filterName) { _ -> }
            }
        }
    }

    fun loadSavedImages(context: Context) {
        viewModelScope.launch {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "FaceFitAR"
            )
            if (directory.exists()) {
                val files = directory.listFiles { file ->
                    file.isFile && (file.extension == "jpg" || file.extension == "jpeg")
                }
                val uris = files?.map { Uri.fromFile(it) } ?: emptyList()
                _capturedImages.value = uris
                if (uris.isNotEmpty()) {
                    _lastCapturedUri.value = uris.last()
                }
            }
        }
    }

    fun removeImage(uri: Uri) {
        _capturedImages.value = _capturedImages.value.filter { it != uri }
        if (_lastCapturedUri.value == uri) {
            _lastCapturedUri.value = _capturedImages.value.lastOrNull()
        }
    }
}
