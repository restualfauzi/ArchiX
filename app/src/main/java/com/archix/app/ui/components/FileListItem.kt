package com.archix.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archix.app.data.model.CategoryType
import com.archix.app.data.model.FileItem
import com.archix.app.ui.theme.FolderYellow
import com.archix.app.ui.theme.TextPrimary
import com.archix.app.ui.theme.TextSecondary
import com.archix.app.utils.FileUtils

@Composable
fun FileListItem(
    fileItem: FileItem,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    showDivider: Boolean = true,
    onToggleSelect: (() -> Unit)? = null,
    onToggleFavorite: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (isSelectionMode && onToggleSelect != null) {
                    onToggleSelect()
                } else {
                    onClick()
                }
            }
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                // Menghapus RoundedCornerShape agar garis divider dapat membentang sempurna
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode && onToggleSelect != null) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Icon / Badge
        FileIconBadge(fileItem = fileItem)

        Spacer(modifier = Modifier.width(12.dp))

        // Name & Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileItem.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val subtitle = if (fileItem.isDirectory) {
                "${fileItem.itemCount} item • ${FileUtils.formatDate(fileItem.lastModified)}"
            } else {
                "${FileUtils.formatFileSize(fileItem.size)} • ${FileUtils.formatDate(fileItem.lastModified)}"
            }

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (onToggleFavorite != null) {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (fileItem.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorit",
                    tint = if (fileItem.isFavorite) FolderYellow else TextSecondary
                )
            }
        }

        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Opsi",
                tint = TextSecondary
            )
        }
    }
    
    // Pembatas tipis ala iOS
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 68.dp), 
            thickness = 0.5.dp, 
            color = Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun FileIconBadge(
    fileItem: FileItem,
    size: Int = 44
) {
    if (fileItem.isDirectory) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .background(color = FolderYellow.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Folder",
                tint = FolderYellow,
                modifier = Modifier.size((size * 0.6).dp)
            )
        }
    } else if (fileItem.isArchive) {
        ArchiveFormatBadge(
            archiveType = fileItem.archiveType,
            extension = fileItem.extension,
            size = size.dp
        )
    } else {
        val (icon, color) = when (fileItem.categoryType) {
            CategoryType.IMAGES -> Icons.Default.Image to Color(0xFF1677FF)
            CategoryType.VIDEO -> Icons.Default.Videocam to Color(0xFF8B5CF6)
            CategoryType.AUDIO -> Icons.Default.MusicNote to Color(0xFFF97316)
            CategoryType.DOCUMENTS -> Icons.Default.Description to Color(0xFFEF4444)
            CategoryType.APK -> Icons.Default.Android to Color(0xFF10B981)
            else -> Icons.Default.Description to Color(0xFF64748B)
        }

        Box(
            modifier = Modifier
                .size(size.dp)
                .background(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = fileItem.name,
                tint = color,
                modifier = Modifier.size((size * 0.55).dp)
            )
        }
    }
}
