package com.mechat.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.mechat.app.ui.theme.LocalMeChatTokens
import com.mechat.app.ui.theme.MeChatTheme

/** Renders a QR code with ZXing (ML Kit-free), drawn in Compose Canvas. */
@Composable
fun QrCode(
    content: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 220.dp,
) {
    val tokens = LocalMeChatTokens.current
    Canvas(modifier = modifier.size(size)) {
        val writer = QRCodeWriter()
        val matrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val scale = size.toPx() / matrix.width
        val path = androidx.compose.ui.graphics.Path().apply {
            fillType = PathFillType.EvenOdd
            for (y in 0 until matrix.height) {
                for (x in 0 until matrix.width) {
                    if (matrix[x, y]) {
                        addRect(
                            androidx.compose.ui.geometry.Rect(
                                x * scale, y * scale, (x + 1) * scale, (y + 1) * scale,
                            ),
                        )
                    }
                }
            }
        }
        drawPath(path, SolidColor(tokens.textPrimary), style = Fill)
    }
}

@Preview(showBackground = true)
@Composable
private fun QrCodePreview() {
    MeChatTheme { QrCode("MCHAT1.example") }
}
