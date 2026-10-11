package com.archix.app.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archix.app.data.model.FileItem
import com.archix.app.ui.theme.PrimaryBlue
import com.archix.app.ui.theme.TextSecondary
import com.archix.app.ui.viewmodel.ArchiveViewModel
import com.archix.app.ui.viewmodel.FileManagerViewModel
import com.archix.app.ui.viewmodel.HomeViewModel
import com.archix.app.ui.viewmodel.MoreViewModel

enum class NavigationTab(
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    HOME("Beranda", Icons.Default.Home, Icons.Outlined.Home),
    FILE("File", Icons.Default.Folder, Icons.Outlined.Folder),
    ARCHIVE("Arsip", Icons.Default.FolderZip, Icons.Outlined.FolderZip),
    MORE("Lainnya", Icons.Default.MoreHoriz, Icons.Outlined.MoreHoriz)
}

@Composable
fun MainContainerScreen(
    homeViewModel: HomeViewModel,
    fileManagerViewModel: FileManagerViewModel,
    archiveViewModel: ArchiveViewModel,
    moreViewModel: MoreViewModel,
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onFileDetailClick: (FileItem) -> Unit,
    onViewArchiveDetails: (FileItem) -> Unit,
    onOpenFile: (FileItem) -> Unit,
    onShareFile: (FileItem) -> Unit
) {
    var selectedTabItem by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            ) {
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE2E8F0)) // Garis batas tipis ala iOS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationTab.entries.forEachIndexed { index, tab ->
                        val isSelected = selectedTabItem == index
                        val iconColor = if (isSelected) PrimaryBlue else TextSecondary
                        val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null, // Hilangkan ripple effect ala iOS
                                    onClick = { selectedTabItem = index }
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = tab.title,
                                tint = iconColor,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = fontWeight,
                                color = iconColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            Crossfade(
                targetState = selectedTabItem,
                animationSpec = tween(durationMillis = 300),
                label = "Tab Transition"
            ) { activeTab ->
                when (activeTab) {
                0 -> HomeScreen(
                    viewModel = homeViewModel,
                    hasStoragePermission = hasStoragePermission,
                    onRequestPermission = onRequestPermission,
                    onCategoryClick = { category ->
                        selectedTabItem = 1 // Switch to File Manager tab
                    },
                    onFolderClick = { folderPath ->
                        if (folderPath.isNotEmpty()) {
                            fileManagerViewModel.navigateToFolder(folderPath)
                        }
                        selectedTabItem = 1 // Switch to File Manager tab
                    },
                    onSearchClick = onSearchClick,
                    onSettingsClick = onSettingsClick,
                    onFileClick = onOpenFile,
                    onFileMoreClick = onFileDetailClick
                )

                1 -> FileManagerScreen(
                    viewModel = fileManagerViewModel,
                    onBackClick = { selectedTabItem = 0 },
                    onSearchClick = onSearchClick,
                    onFileDetailClick = onFileDetailClick,
                    onOpenFile = onOpenFile,
                    onShareFile = onShareFile
                )

                2 -> ArchiveScreen(
                    viewModel = archiveViewModel,
                    onSearchClick = onSearchClick,
                    onViewArchiveDetails = onViewArchiveDetails,
                    onFileDetailClick = onFileDetailClick,
                    onShareArchive = onShareFile,
                    onCreateArchiveClick = {
                        selectedTabItem = 1 // Go to File Manager to select files
                    }
                )

                3 -> MoreScreen(
                    viewModel = moreViewModel,
                    onSettingsClick = onSettingsClick,
                    onAboutClick = onAboutClick,
                    onFileClick = onOpenFile,
                    onFileMoreClick = onFileDetailClick
                )
            }
            }
        }
    }
}
