package com.archix.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archix.app.data.model.CategoryType
import com.archix.app.ui.theme.TextPrimary
import com.archix.app.ui.theme.TextSecondary

@Composable
fun CategoryCard(
    categoryType: CategoryType,
    itemCount: Int,
    totalSizeBytes: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (categoryType) {
        CategoryType.IMAGES -> Icons.Default.Image to Color(0xFF1677FF)
        CategoryType.VIDEO -> Icons.Default.Videocam to Color(0xFF8B5CF6)
        CategoryType.AUDIO -> Icons.Default.MusicNote to Color(0xFFF97316)
        CategoryType.DOCUMENTS -> Icons.Default.Description to Color(0xFFEF4444)
        CategoryType.APK -> Icons.Default.Android to Color(0xFF10B981)
        CategoryType.ARCHIVES -> Icons.Default.FolderZip to Color(0xFF3B82F6)
        CategoryType.OTHERS -> Icons.Default.MoreHoriz to Color(0xFF64748B)
    }

    val title = categoryType.titleId

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(105.dp) // Fixed height to prevent breaking alignment
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Dibuat flat
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color = color.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (itemCount > 0) "$itemCount file" else " ",
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
