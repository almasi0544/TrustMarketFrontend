package com.trustmarket.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.network.friendlyErrorMessage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

private val TrustTeal = Color(0xFF1F4E5F)
private val ALLOWED_MIME = setOf("image/jpeg", "image/png", "image/webp", "video/mp4", "video/quicktime")

@Composable
fun MediaPickerField(
    token: String?,
    onUploaded: (mediaUrl: String, mediaType: String) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var previewUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var uploadedType by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null || token == null) return@rememberLauncherForActivityResult
        val mimeType = context.contentResolver.getType(uri) ?: ""
        if (mimeType !in ALLOWED_MIME) {
            onError("Please choose a JPEG/PNG/WEBP image or an MP4/MOV video.")
            return@rememberLauncherForActivityResult
        }
        previewUri = uri
        uploading = true
        scope.launch {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("Could not read file")
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", "upload_${System.currentTimeMillis()}", requestBody)
                val result = RetrofitClient.api.uploadMedia("Bearer $token", part)
                uploadedType = result.media_type
                onUploaded(result.media_url, result.media_type)
            } catch (e: Exception) {
                previewUri = null
                onError(friendlyErrorMessage(e))
            } finally {
                uploading = false
            }
        }
    }

    Column {
        OutlinedButton(
            onClick = { launcher.launch("*/*") },
            enabled = !uploading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    uploading -> "Uploading..."
                    previewUri != null -> "Change photo/video"
                    else -> "Add photo or video (required)"
                }
            )
        }

        if (previewUri != null && uploadedType == "image") {
            Spacer(Modifier.height(8.dp))
            AsyncImage(
                model = previewUri,
                contentDescription = "Preview",
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
            )
        } else if (previewUri != null && uploadedType == "video") {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🎬 Video attached", color = TrustTeal) }
        }
    }
}