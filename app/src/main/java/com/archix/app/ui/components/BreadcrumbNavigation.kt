package com.archix.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archix.app.ui.theme.PrimaryBlue
import com.archix.app.ui.theme.TextPrimary
import com.archix.app.ui.theme.TextSecondary
import java.io.File

@Composable
fun BreadcrumbNavigation(
    breadcrumbs: List<File>,
    onFolderClick: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(breadcrumbs) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        breadcrumbs.forEachIndexed { index, folder ->
            val isLast = index == breadcrumbs.size - 1
            val isRoot = index == 0

            Surface(
                onClick = { onFolderClick(folder) },
                shape = RoundedCornerShape(8.dp),
                color = if (isLast) PrimaryBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRoot) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Beranda",
                            tint = if (isLast) PrimaryBlue else TextSecondary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "Beranda",
                            fontSize = 13.sp,
                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                            color = if (isLast) PrimaryBlue else TextPrimary
                        )
                    } else {
                        Text(
                            text = folder.name,
                            fontSize = 13.sp,
                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                            color = if (isLast) PrimaryBlue else TextPrimary
                        )
                    }
                }
            }

            if (!isLast) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}
