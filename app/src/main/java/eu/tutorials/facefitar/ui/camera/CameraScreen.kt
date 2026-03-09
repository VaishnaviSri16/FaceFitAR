package eu.tutorials.facefitar.ui.camera

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import eu.tutorials.facefitar.camera.CameraManager
import eu.tutorials.facefitar.data.repository.CaptureRecord
import eu.tutorials.facefitar.ui.components.FilterCarousel
import eu.tutorials.facefitar.ui.overlay.FaceOverlayView
import eu.tutorials.facefitar.viewmodel.AuthViewModel
import eu.tutorials.facefitar.viewmodel.FaceFilterViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CameraScreen(
    cameraManager: CameraManager,
    faceFilterViewModel: FaceFilterViewModel,
    authViewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    val overlayView = remember { FaceOverlayView(context) }

    val detectedFaces by faceFilterViewModel.detectedFaces.collectAsState()
    val selectedFilter by faceFilterViewModel.selectedFilter.collectAsState()
    val lastCapturedUri by faceFilterViewModel.lastCapturedUri.collectAsState()
    val capturedImages by faceFilterViewModel.capturedImages.collectAsState()
    val captureHistory by faceFilterViewModel.captureHistory.collectAsState()

    var inputWidth by remember { mutableIntStateOf(0) }
    var inputHeight by remember { mutableIntStateOf(0) }
    var imageRotation by remember { mutableIntStateOf(0) }
    
    var showImageDetail by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var showGallery by remember { mutableStateOf(false) }

    // Initial load
    LaunchedEffect(Unit) {
        faceFilterViewModel.loadSavedImages(context)
    }

    // Start camera
    LaunchedEffect(previewViewRef, isFrontCamera) {
        previewViewRef?.let { previewView ->
            cameraManager.startCamera(context, lifecycleOwner, previewView, isFrontCamera) { faces, width, height, rotation ->
                faceFilterViewModel.onFacesDetected(faces)
                inputWidth = width
                inputHeight = height
                imageRotation = rotation
                overlayView.updateFaces(faces, width, height, selectedFilter, isFrontCamera, rotation)
            }
        }
    }

    // Update overlay
    LaunchedEffect(detectedFaces, selectedFilter, isFrontCamera) {
        overlayView.updateFaces(detectedFaces, inputWidth, inputHeight, selectedFilter, isFrontCamera, imageRotation)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        AndroidView(
            factory = { overlayView },
            modifier = Modifier.fillMaxSize()
        )

        // Top Control Row (Logout only)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterCarousel(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { filter ->
                        faceFilterViewModel.selectFilter(filter)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Gallery Button (Bottom Left)
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable {
                                showGallery = true
                            },
                        color = Color.Black.copy(alpha = 0.4f)
                    ) {
                        if (lastCapturedUri != null) {
                            AsyncImage(
                                model = lastCapturedUri,
                                contentDescription = "Last captured",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                tint = Color.White,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Capture Button (Center)
                    FloatingActionButton(
                        onClick = {
                            previewViewRef?.bitmap?.let { bitmap ->
                                val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                                val canvas = android.graphics.Canvas(mutableBitmap)
                                overlayView.drawFilters(canvas, mutableBitmap.width.toFloat(), mutableBitmap.height.toFloat(), true)
                                
                                val currentFilterName = selectedFilter?.name ?: "Original"
                                saveBitmapToGallery(context, mutableBitmap) { uri ->
                                    faceFilterViewModel.setLastCapturedUri(uri, currentFilterName)
                                }
                            }
                        },
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        shape = CircleShape,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Flip Camera Button (Bottom Right)
                    IconButton(
                        onClick = { isFrontCamera = !isFrontCamera },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.FlipCameraAndroid, 
                            contentDescription = "Flip Camera", 
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        if (showGallery) {
            GalleryDialog(
                images = capturedImages,
                history = captureHistory,
                onClose = { showGallery = false },
                onImageClick = { index ->
                    selectedImageIndex = index
                    showImageDetail = true
                }
            )
        }

        if (showImageDetail) {
            val reversedImages = capturedImages.reversed()
            if (reversedImages.isNotEmpty()) {
                ImageDetailDialog(
                    images = reversedImages,
                    initialIndex = selectedImageIndex,
                    onClose = { 
                        showImageDetail = false
                    },
                    onDelete = { uri ->
                        deleteImage(context, uri)
                        faceFilterViewModel.removeImage(uri)
                        if (capturedImages.size <= 1) {
                            showImageDetail = false
                        }
                    },
                    onShare = { uri ->
                        shareImage(context, uri)
                    }
                )
            } else {
                showImageDetail = false
            }
        }
    }
}

@Composable
fun GalleryDialog(
    images: List<Uri>,
    history: List<CaptureRecord>,
    onClose: () -> Unit,
    onImageClick: (Int) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Gallery & History", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = Color.White)
                    }
                }

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { 
                            scope.launch { pagerState.animateScrollToPage(0) }
                        },
                        text = { Text("Photos") }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { 
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        text = { Text("Cloud History") }
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    if (page == 0) {
                        // Local Photos Tab
                        if (images.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No images yet", color = Color.White.copy(alpha = 0.6f))
                            }
                        } else {
                            val reversedImages = images.reversed()
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                contentPadding = PaddingValues(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(reversedImages) { index, uri ->
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onImageClick(index) },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    } else {
                        // Firestore History Tab
                        if (history.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No cloud records found", color = Color.White.copy(alpha = 0.6f))
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(history) { record ->
                                    CaptureHistoryItem(record)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CaptureHistoryItem(record: CaptureRecord) {
    val date = remember(record.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        sdf.format(Date(record.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = record.filterName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = date,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
            
            Surface(
                color = if (record.status == "Success") Color(0xFF4CAF50) else Color.Red,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = record.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ImageDetailDialog(
    images: List<Uri>,
    initialIndex: Int,
    onClose: () -> Unit,
    onDelete: (Uri) -> Unit,
    onShare: (Uri) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { images.size }
    
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                AsyncImage(
                    model = images[page],
                    contentDescription = "Full Screen Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                
                Row {
                    IconButton(
                        onClick = { onShare(images[pagerState.currentPage]) },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onDelete(images[pagerState.currentPage]) },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                }
            }
        }
    }
}

private fun saveBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
    onSaved: (uri: Uri?) -> Unit
) {
    val filename = "FaceFitAR_${System.currentTimeMillis()}.jpg"
    try {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/FaceFitAR")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let { uri ->
            resolver.openOutputStream(uri)?.use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            onSaved(uri)
            Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onSaved(null)
    }
}

private fun deleteImage(context: Context, uri: Uri) {
    try {
        if (uri.scheme == "content") {
            context.contentResolver.delete(uri, null, null)
        } else if (uri.scheme == "file") {
            val file = File(uri.path ?: return)
            if (file.exists()) {
                file.delete()
            }
        }
        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
    }
}

private fun shareImage(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Image"))
}
