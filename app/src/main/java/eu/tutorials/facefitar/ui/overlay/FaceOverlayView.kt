package eu.tutorials.facefitar.ui.overlay

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import eu.tutorials.facefitar.filters.FilterModel
import eu.tutorials.facefitar.filters.FilterType
import kotlin.math.pow
import kotlin.math.sqrt

class FaceOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var faces: List<Face> = emptyList()
    private var selectedFilter: FilterModel? = null
    private var imageRotation: Int = 0
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var isFrontCamera: Boolean = true

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val reusableRect = RectF()
    private val reusableSrc = Rect()
    private val bitmapCache = mutableMapOf<Int, Bitmap?>()

    init {
        // Ensure the overlay doesn't intercept touches so buttons underneath/above work
        isClickable = false
        isFocusable = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Explicitly return false to pass touches to layers below
        return false
    }

    private fun getFilterBitmap(resId: Int): Bitmap? {
        return bitmapCache.getOrPut(resId) {
            BitmapFactory.decodeResource(resources, resId)
        }
    }

    fun updateFaces(newFaces: List<Face>, width: Int, height: Int, filter: FilterModel?, isFront: Boolean, rotation: Int = 0) {
        faces = newFaces
        imageWidth = width
        imageHeight = height
        selectedFilter = filter
        isFrontCamera = isFront
        imageRotation = rotation
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawFilters(canvas, width.toFloat(), height.toFloat())
    }

    fun drawFilters(canvas: Canvas, viewWidth: Float, viewHeight: Float, isCapture: Boolean = false) {
        // 0. Handle Original filter (no filter)
        if (selectedFilter?.type == FilterType.ORIGINAL) {
            return
        }

        // 1. Handle full-screen filters like Sparkle first.
        // These are drawn once as a static overlay and don't move with the face.
        if (selectedFilter?.type == FilterType.SPARKLE) {
            val bmp = getFilterBitmap(selectedFilter!!.filterRes) ?: return
            
            // To hide brand text/watermarks at the bottom, we crop the bottom 10% of the asset
            reusableSrc.set(0, 0, bmp.width, (bmp.height * 0.90f).toInt())
            
            // Stretch to fill the entire screen/capture area like a picture frame
            reusableRect.set(0f, 0f, viewWidth, viewHeight)
            
            canvas.drawBitmap(bmp, reusableSrc, reusableRect, paint)
            return
        }

        if (imageWidth == 0 || imageHeight == 0) return

        val isPortrait = imageRotation == 90 || imageRotation == 270
        val scaleX = if (isPortrait) viewWidth / imageHeight else viewWidth / imageWidth
        val scaleY = if (isPortrait) viewHeight / imageWidth else viewHeight / imageHeight

        for (face in faces) {
            if (selectedFilter == null) {
                drawDebugInfo(canvas, face, scaleX, scaleY, viewWidth)
            } else {
                drawBitmapFilter(canvas, face, scaleX, scaleY, viewWidth, selectedFilter!!)
            }
        }
    }

    private fun translateX(x: Float, scaleX: Float, viewWidth: Float): Float {
        return if (isFrontCamera) {
            viewWidth - (x * scaleX)
        } else {
            x * scaleX
        }
    }

    private fun drawDebugInfo(canvas: Canvas, face: Face, scaleX: Float, scaleY: Float, viewWidth: Float) {
        paint.reset()
        paint.isAntiAlias = true
        paint.color = Color.GREEN
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        val box = face.boundingBox
        
        val left = translateX(box.left.toFloat(), scaleX, viewWidth)
        val right = translateX(box.right.toFloat(), scaleX, viewWidth)
        
        canvas.drawRect(
            minOf(left, right),
            box.top * scaleY,
            maxOf(left, right),
            box.bottom * scaleY,
            paint
        )
    }

    private fun drawBitmapFilter(canvas: Canvas, face: Face, scaleX: Float, scaleY: Float, viewWidth: Float, filter: FilterModel) {
        val bitmap = getFilterBitmap(filter.filterRes) ?: return
        val box = face.boundingBox
        
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
        val noseBase = face.getLandmark(FaceLandmark.NOSE_BASE)

        var anchorX = translateX(box.centerX().toFloat(), scaleX, viewWidth)
        var anchorY = box.centerY() * scaleY
        var filterWidth = box.width() * scaleX
        
        if (leftEye != null && rightEye != null) {
            val lx = translateX(leftEye.position.x, scaleX, viewWidth)
            val rx = translateX(rightEye.position.x, scaleX, viewWidth)
            val ly = leftEye.position.y * scaleY
            val ry = rightEye.position.y * scaleY
            
            val eyeMidX = (lx + rx) / 2f
            val eyeMidY = (ly + ry) / 2f
            val eyeDist = sqrt((lx - rx).pow(2) + (ly - ry).pow(2))
            
            when (filter.type) {
                FilterType.CROWN -> {
                    anchorX = eyeMidX
                    // Adjust only the specific "Crown" filter higher (onto hair)
                    // while keeping Rose and Heart crowns at their current correct positions.
                    val verticalMultiplier = if (filter.name == "Crown") 2.8f else 2.0f
                    anchorY = eyeMidY - eyeDist * verticalMultiplier
                    filterWidth = eyeDist * 4.4f
                }
                FilterType.EARS -> {
                    // Optimized EARS logic for Bunny, Dog, and Cat filters
                    anchorX = eyeMidX
                    // Reduced size to fit better on the head
                    filterWidth = eyeDist * 4.2f 
                    val filterHeight = (filterWidth * bitmap.height) / bitmap.width
                    
                    // Determine how much to shift the filter up based on the specific animal
                    // Cat filter was appearing too high (on eyes) at 0.45f, so we lowered it to 0.32f
                    val verticalShift = if (filter.name.contains("Cat", true)) 0.32f else 0.38f
                    
                    anchorY = if (noseBase != null) {
                        val ny = noseBase.position.y * scaleY
                        // Shift center upwards so the nose piece aligns with the human nose
                        ny - (filterHeight * verticalShift) 
                    } else {
                        // Fallback: place it slightly above the eye line
                        eyeMidY - (eyeDist * 1.0f)
                    }
                }
                FilterType.GLASSES -> {
                    anchorX = eyeMidX
                    anchorY = eyeMidY
                    filterWidth = eyeDist * 3.65f
                }
                FilterType.MASK -> {
                    anchorX = eyeMidX
                    if (filter.name.contains("Clown", true)) {
                        anchorY = eyeMidY + eyeDist * 1f
                        filterWidth = eyeDist * 4.6f
                    } else {
                        anchorY = if (noseBase != null) (noseBase.position.y * scaleY) + (eyeDist * 0.88f) else eyeMidY + eyeDist * 2.4f
                        filterWidth = eyeDist * 4.6f
                    }
                }
                else -> {}
            }
        }

        val filterHeight = (filterWidth * bitmap.height) / bitmap.width

        canvas.save()
        canvas.translate(anchorX, anchorY)
        // Fixed rotation: For front camera (mirrored), use headEulerAngleZ as is.
        // For back camera, invert it to convert ML Kit's CCW-positive to Canvas CW-positive.
        val rotation = if (isFrontCamera) face.headEulerAngleZ else -face.headEulerAngleZ
        canvas.rotate(rotation)
        
        reusableRect.set(-filterWidth / 2f, -filterHeight / 2f, filterWidth / 2f, filterHeight / 2f)
        canvas.drawBitmap(bitmap, null, reusableRect, paint)
        
        canvas.restore()
    }
}
