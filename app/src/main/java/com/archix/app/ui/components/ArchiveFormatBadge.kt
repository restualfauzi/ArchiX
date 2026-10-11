package com.archix.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archix.app.data.model.ArchiveType
import com.archix.app.ui.theme.RarPurple
import com.archix.app.ui.theme.SevenZGreen
import com.archix.app.ui.theme.SlateGray
import com.archix.app.ui.theme.ZipBlue

@Composable
fun ArchiveFormatBadge(
    archiveType: ArchiveType?,
    extension: String = "",
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val (badgeColor, label) = when (archiveType) {
        ArchiveType.ZIP -> ZipBlue to "ZIP"
        ArchiveType.RAR -> RarPurple to "RAR"
        ArchiveType.SEVEN_Z -> SevenZGreen to "7Z"
        ArchiveType.TAR -> SlateGray to "TAR"
        ArchiveType.GZ -> SlateGray to "GZ"
        null -> SlateGray to extension.uppercase().take(4).ifEmpty { "FILE" }
    }

    Box(
        modifier = modifier
            .size(size)
            .background(color = badgeColor, shape = RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = if (label.length > 3) 10.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(2.dp)
        )
    }
}
