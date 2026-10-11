package com.archix.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archix.app.data.model.CategoryType
import com.archix.app.data.model.FileItem
import com.archix.app.ui.components.ArchiXLogo
import com.archix.app.ui.components.CategoryCard
import com.archix.app.ui.components.FileListItem
import com.archix.app.ui.components.PermissionBanner
import com.archix.app.ui.components.StorageCard
import com.archix.app.ui.theme.DarkBlue
import com.archix.app.ui.theme.PrimaryBlue
import com.archix.app.ui.theme.TextPrimary
import com.archix.app.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit,
    onCategoryClick: (CategoryType) -> Unit,
    onFolderClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFileClick: (FileItem) -> Unit,
    onFileMoreClick: (FileItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ArchiXLogo(size = 38.dp)
                        Spacer(modifier = Modifier.padding(start = 10.dp))
                        Text(
                            text = "ArchiX",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = DarkBlue
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = TextPrimary
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp) // Lebih luas
        ) {
            // Permission Request Banner if needed
            if (!hasStoragePermission) {
                item {
                    PermissionBanner(onRequestPermission = onRequestPermission)
                }
            }

            // Storage Card
            item {
                StorageCard(storageInfo = uiState.storageInfo)
            }

            // Categories Header
            item {
                Text(
                    text = "Kategori",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
            }

            // Categories 2-Row Layout (Zero Clipping)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val row1 = listOf(
                            CategoryType.IMAGES,
                            CategoryType.VIDEO,
                            CategoryType.AUDIO,
                            CategoryType.DOCUMENTS
                        )
                        row1.forEach { cat ->
                            val countData = uiState.categoryCounts[cat] ?: Pair(0, 0L)
                            CategoryCard(
                                categoryType = cat,
                                itemCount = countData.first,
                                totalSizeBytes = countData.second,
                                onClick = { onCategoryClick(cat) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val row2 = listOf(
                            CategoryType.APK,
                            CategoryType.ARCHIVES,
                            CategoryType.OTHERS
                        )
                        row2.forEach { cat ->
                            val countData = uiState.categoryCounts[cat] ?: Pair(0, 0L)
                            CategoryCard(
                                categoryType = cat,
                                itemCount = countData.first,
                                totalSizeBytes = countData.second,
                                onClick = { onCategoryClick(cat) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Spacer to keep 4-column alignment
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Folder Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Folder",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    TextButton(onClick = { onFolderClick("") }) {
                        Text(
                            text = "Lihat semua",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Primary Folders List
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                ) {
                    uiState.primaryFolders.forEachIndexed { index, folder ->
                        FileListItem(
                            fileItem = folder,
                            onClick = { onFolderClick(folder.path) },
                            onMoreClick = { onFileMoreClick(folder) },
                            showDivider = index < uiState.primaryFolders.size - 1
                        )
                    }
                }
            }

            // Recent Files Section Header (if available)
            if (uiState.recentFiles.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "File Terbaru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    ) {
                        val recentList = uiState.recentFiles.take(5)
                        recentList.forEachIndexed { index, recentFile ->
                            FileListItem(
                                fileItem = recentFile,
                                onClick = { onFileClick(recentFile) },
                                onMoreClick = { onFileMoreClick(recentFile) },
                                showDivider = index < recentList.size - 1
                            )
                        }
                    }
                }
            }
        }
    }
}
