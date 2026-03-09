package eu.tutorials.facefitar.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class CaptureRecord(
    val id: String = "",
    val userId: String = "",
    val filterName: String = "",
    val timestamp: Long = 0,
    val status: String = "Success"
)

class HistoryRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun logCapture(filterName: String, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("HistoryRepository", "Cannot log capture: User not logged in")
            onComplete(false)
            return
        }
        
        val record = hashMapOf(
            "userId" to userId,
            "filterName" to filterName,
            "timestamp" to System.currentTimeMillis(),
            "status" to "Success"
        )

        firestore.collection("captures")
            .add(record)
            .addOnSuccessListener { 
                Log.d("HistoryRepository", "Capture logged successfully to Firestore")
                onComplete(true) 
            }
            .addOnFailureListener { e -> 
                Log.e("HistoryRepository", "Error logging capture", e)
                onComplete(false) 
            }
    }

    /**
     * Using a Flow with a real-time snapshot listener.
     * This ensures the UI updates IMMEDIATELY as soon as Firestore changes.
     */
    fun getCaptureHistoryFlow(): Flow<List<CaptureRecord>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("captures")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HistoryRepository", "Snapshot listener failed", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val records = snapshot.documents.map { doc ->
                        CaptureRecord(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            filterName = doc.getString("filterName") ?: "Unknown",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            status = doc.getString("status") ?: "Success"
                        )
                    }
                    // Sort descending in memory to avoid index requirements
                    trySend(records.sortedByDescending { it.timestamp })
                }
            }

        awaitClose { listener.remove() }
    }

    // Keep this for one-time fetches if needed
    suspend fun getCaptureHistory(): List<CaptureRecord> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("captures")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val records = snapshot.documents.map { doc ->
                CaptureRecord(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    filterName = doc.getString("filterName") ?: "Unknown",
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    status = doc.getString("status") ?: "Success"
                )
            }
            records.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
