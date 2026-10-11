package com.archix.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archix.app.data.model.FileItem
import com.archix.app.ui.components.EmptyState
import com.archix.app.ui.components.FileListItem
import com.archix.app.ui.components.ProgressDialog
import com.archix.app.ui.theme.DarkBlue
import com.archix.app.ui.theme.PrimaryBlue
import com.archix.app.ui.theme.TextPrimary
import com.archix.app.ui.viewmodel.ArchiveFilterTab
import com.archix.app.ui.viewmodel.ArchiveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel,
    onSearchClick: () -> Unit,
    onViewArchiveDetails: (FileItem) -> Unit,
    onFileDetailClick: (FileItem) -> Unit,
    onShareArchive: (FileItem) -> Unit,
    onCreateArchiveClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedArchiveForAction by remember { mutableStateOf<FileItem?>(null) }
    var showActionSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        uiState.error?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Arsip",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DarkBlue
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Cari")
                    }
                    IconButton(onClick = { viewModel.loadArchives() }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Opsi")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateArchiveClick,
                containerColor = PrimaryBlue,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Buat Arsip Baru")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Tabs (Semua, ZIP, RAR, 7Z, Lainnya)
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                ArchiveFilterTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (uiState.filteredArchives.isEmpty()) {
                EmptyState(
                    title = "Tidak Ada Arsip",
                    description = "Tidak ditemukan file arsip pada kategori '${uiState.selectedTab.title}'."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        ) {
                            uiState.filteredArchives.forEach { archive ->
                                FileListItem(
                                    fileItem = archive,
                                    onClick = {
                                        onViewArchiveDetails(archive)
                                    },
                                    onMoreClick = {
                                        selectedArchiveForAction = archive
                                        showActionSheet = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Extraction Progress Dialog
    if (uiState.isExtracting) {
        ProgressDialog(
            title = "Mengekstrak Arsip",
            statusText = uiState.currentExtractingFile,
            progress = uiState.extractionProgress
        )
    }

    // Archive Action Bottom Sheet
    if (showActionSheet && selectedArchiveForAction != null) {
        val archive = selectedArchiveForAction!!
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = archive.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                DropdownMenuItem(
                    text = { Text("Ekstrak di sini") },
                    onClick = {
                        showActionSheet = false
                        viewModel.extractHere(archive)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Lihat isi arsip") },
                    onClick = {
                        showActionSheet = false
                        onViewArchiveDetails(archive)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Bagikan arsip") },
                    onClick = {
                        showActionSheet = false
                        onShareArchive(archive)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Detail file") },
                    onClick = {
                        showActionSheet = false
                        onFileDetailClick(archive)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Hapus arsip", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showActionSheet = false
                        viewModel.deleteArchive(archive)
                    }
                )
            }
        }
    }
}
