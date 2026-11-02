package com.mechat.app.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.mechat.app.ui.components.GradientBackground
import com.mechat.app.ui.components.RoundIconButton
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.Spacing
import java.util.concurrent.Executors

/**
 * QR scanner: CameraX + zxing-core (no ML Kit). CAMERA permission is requested
 * at runtime ONLY here, when the user tapped Scan (RULE 2).
 */
@Composable
fun ScanQrScreen(
    onScanned: (String) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val tokens = LocalMeChatTokens.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    GradientBackground {
        Box(Modifier.fillMaxSize()) {
            if (hasPermission) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val provider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                            analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                val buffer = imageProxy.planes[0].buffer
                                val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
                                val source = PlanarYUVLuminanceSource(
                                    bytes, imageProxy.width, imageProxy.height, 0, 0,
                                    imageProxy.width, imageProxy.height, false,
                                )
                                runCatching {
                                    MultiFormatReader().decode(
                                        BinaryBitmap(HybridBinarizer(source)),
                                    ).text
                                }.getOrNull()?.let { onScanned(it) }
                                imageProxy.close()
                            }
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis,
                            )
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Camera permission needed to scan", color = tokens.textSecondary)
                }
            }
            RoundIconButton(
                icon = Icons.Default.Close,
                contentDescription = "Close scanner",
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.md),
                containerColor = tokens.glassSurface,
            )
        }
    }
}
