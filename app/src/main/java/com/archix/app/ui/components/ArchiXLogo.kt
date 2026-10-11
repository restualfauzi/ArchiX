package com.archix.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.archix.app.ui.theme.DarkBlue
import com.archix.app.ui.theme.FolderYellow
import com.archix.app.ui.theme.PrimaryBlue

@Composable
fun ArchiXLogo(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val width = size.toPx()
        val height = size.toPx()
        val corner = width * 0.22f

        // Draw background squircle
        drawRoundRect(
            color = DarkBlue,
            topLeft = Offset.Zero,
            size = Size(width, height),
            cornerRadius = CornerRadius(corner, corner)
        )

        // Draw blue gradient accent path
        val accentPath = Path().apply {
            moveTo(0f, height * 0.5f)
            lineTo(width, 0f)
            lineTo(width, height)
            close()
        }
        clipPath(accentPath) {
            drawRoundRect(
                color = PrimaryBlue.copy(alpha = 0.3f),
                topLeft = Offset.Zero,
                size = Size(width, height)
            )
        }

        // Draw Geometric "A"
        val leftLeg = Path().apply {
            moveTo(width * 0.22f, height * 0.82f)
            lineTo(width * 0.48f, height * 0.18f)
            lineTo(width * 0.58f, height * 0.18f)
            lineTo(width * 0.40f, height * 0.82f)
            close()
        }
        drawPath(leftLeg, Color.White)

        val rightLeg = Path().apply {
            moveTo(width * 0.78f, height * 0.82f)
            lineTo(width * 0.52f, height * 0.18f)
            lineTo(width * 0.42f, height * 0.18f)
            lineTo(width * 0.60f, height * 0.82f)
            close()
        }
        drawPath(rightLeg, Color(0xFFE2E8F0))

        // Draw Folder Accent in center counter of "A"
        val folderTab = Path().apply {
            moveTo(width * 0.32f, height * 0.54f)
            lineTo(width * 0.46f, height * 0.54f)
            lineTo(width * 0.50f, height * 0.58f)
            lineTo(width * 0.68f, height * 0.58f)
            lineTo(width * 0.72f, height * 0.78f)
            lineTo(width * 0.32f, height * 0.78f)
            close()
        }
        drawPath(folderTab, FolderYellow)

        val folderFlap = Path().apply {
            moveTo(width * 0.30f, height * 0.62f)
            lineTo(width * 0.70f, height * 0.62f)
            lineTo(width * 0.66f, height * 0.80f)
            lineTo(width * 0.34f, height * 0.80f)
            close()
        }
        drawPath(folderFlap, PrimaryBlue)
    }
}
