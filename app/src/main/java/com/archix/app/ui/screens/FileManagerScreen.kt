package com.archix.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archix.app.data.model.FileItem
import com.archix.app.data.model.SortType
import com.archix.app.data.model.ViewMode
import com.archix.app.ui.components.BreadcrumbNavigation
import com.archix.app.ui.components.ConfirmDialog
import com.archix.app.ui.components.EmptyState
import com.archix.app.ui.components.FileGridItem
import com.archix.app.ui.components.FileListItem
import com.archix.app.ui.components.ProgressDialog
import com.archix.app.ui.theme.DarkBlue
import com.archix.app.ui.theme.PrimaryBlue
import com.archix.app.ui.theme.TextPrimary
import com.archix.app.ui.viewmodel.FileManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    viewModel: FileManagerViewModel,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFileDetailClick: (FileItem) -> Unit,
    onOpenFile: (FileItem) -> Unit,
    onShareFile: (FileItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSortMenu by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showCompressDialog by remember { mutableStateOf(false) }
    var selectedItemForAction by remember { mutableStateOf<FileItem?>(null) }
    var newFolderNameInput by remember { mutableStateOf("") }
    var newNameInput by remember { mutableStateOf("") }
    var archiveNameInput by remember { mutableStateOf("") }
    var showActionSheet by remember { mutableStateOf(false) }

    val isSelectionMode = uiState.selectedFiles.isNotEmpty()
    val isCopyingOrMoving = uiState.isCopyingOrMoving

    BackHandler(enabled = uiState.currentDirectory.absolutePath != viewModel.getRootDirectory().absolutePath || isSelectionMode || isCopyingOrMoving) {
        when {
            isSelectionMode -> viewModel.clearSelection()
            isCopyingOrMoving -> viewModel.cancelCopyOrMove()
            else -> viewModel.navigateUp()
        }
    }

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
                        text = if (isSelectionMode) "${uiState.selectedFiles.size} dipilih" else uiState.currentDirectory.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DarkBlue
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        if (!viewModel.navigateUp()) {
                            onBackClick()
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { viewModel.selectAll() }) {
                            Icon(imageVector = Icons.Default.SelectAll, contentDescription = "Pilih Semua")
                        }
                        IconButton(onClick = { viewModel.prepareCopy() }) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Salin")
                        }
                        IconButton(onClick = { viewModel.prepareCut() }) {
                            Icon(imageVector = Icons.Default.ContentCut, contentDescription = "Potong")
                        }
                        IconButton(onClick = { showCompressDialog = true; archiveNameInput = "Archive" }) {
                            Icon(imageVector = Icons.Default.FolderZip, contentDescription = "Kompres ZIP")
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus")
                        }
                    } else if (isCopyingOrMoving) {
                        IconButton(onClick = { viewModel.pasteClipboard() }) {
                            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Tempel (Paste)")
                        }
                        IconButton(onClick = { viewModel.cancelCopyOrMove() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Batal")
                        }
                    } else {
                        IconButton(onClick = { showCreateFolderDialog = true; newFolderNameInput = "" }) {
                            Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "Buat Folder")
                        }
                        IconButton(onClick = { viewModel.setViewMode(if (uiState.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST) }) {
                            Icon(
                                imageVector = if (uiState.viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                                contentDescription = "Ubah Tampilan"
                            )
                        }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = "Urutkan")
                        }
                        IconButton(onClick = onSearchClick) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Cari")
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortType.entries.forEach { sortType ->
                                DropdownMenuItem(
                                    text = { Text(sortType.titleId) },
                                    onClick = {
                                        viewModel.setSortType(sortType)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { showCreateFolderDialog = true; newFolderNameInput = "" },
                    containerColor = PrimaryBlue,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Folder")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Breadcrumbs Navigation Bar
            BreadcrumbNavigation(
                breadcrumbs = uiState.breadcrumbs,
                onFolderClick = { file -> viewModel.loadDirectory(file) }
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (uiState.files.isEmpty()) {
                EmptyState(
                    title = "Folder Kosong",
                    description = "Tidak ada file atau folder di direktori ini."
                )
            } else {
                if (uiState.viewMode == ViewMode.LIST) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            ) {
                                uiState.files.forEach { item ->
                                    FileListItem(
                                        fileItem = item,
                                        isSelected = uiState.selectedFiles.contains(item),
                                        isSelectionMode = isSelectionMode,
                                        onClick = {
                                            if (item.isDirectory) {
                                                viewModel.navigateToFolder(item.path)
                                            } else {
                                                onOpenFile(item)
                                            }
                                        },
                                        onMoreClick = {
                                            selectedItemForAction = item
                                            showActionSheet = true
                                        },
                                        onToggleSelect = { viewModel.toggleFileSelection(item) },
                                        onToggleFavorite = { viewModel.toggleFavorite(item) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.files) { item ->
                            FileGridItem(
                                fileItem = item,
                                isSelected = uiState.selectedFiles.contains(item),
                                onClick = {
                                    if (item.isDirectory) {
                                        viewModel.navigateToFolder(item.path)
                                    } else {
                                        onOpenFile(item)
                                    }
                                },
                                onMoreClick = {
                                    selectedItemForAction = item
                                    showActionSheet = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Operation Progress Dialog
    if (uiState.isOperating) {
        ProgressDialog(
            title = "Memproses",
            statusText = uiState.operationStatus,
            progress = uiState.operationProgress
        )
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Buat Folder Baru", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newFolderNameInput,
                    onValueChange = { newFolderNameInput = it },
                    label = { Text("Nama Folder") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderNameInput.isNotBlank()) {
                            viewModel.createFolder(newFolderNameInput.trim())
                            showCreateFolderDialog = false
                        }
                    }
                ) {
                    Text("Buat")
                }
            },
            dismissButton = {
                Button(onClick = { showCreateFolderDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Rename Dialog
    if (showRenameDialog && selectedItemForAction != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Ganti Nama", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newNameInput,
                    onValueChange = { newNameInput = it },
                    label = { Text("Nama Baru") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedItemForAction?.let { item ->
                            if (newNameInput.isNotBlank()) {
                                viewModel.renameFile(item, newNameInput.trim())
                                showRenameDialog = false
                            }
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                Button(onClick = { showRenameDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        ConfirmDialog(
            title = "Hapus File",
            message = if (selectedItemForAction != null) "Apakah Anda yakin ingin menghapus '${selectedItemForAction?.name}'?" else "Apakah Anda yakin ingin menghapus ${uiState.selectedFiles.size} item yang dipilih?",
            isDestructive = true,
            confirmText = "Hapus",
            onConfirm = {
                showDeleteConfirmDialog = false
                if (selectedItemForAction != null) {
                    viewModel.deleteFile(selectedItemForAction!!)
                    selectedItemForAction = null
                } else {
                    viewModel.deleteSelectedFiles()
                }
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    // Compress ZIP Dialog
    if (showCompressDialog) {
        AlertDialog(
            onDismissRequest = { showCompressDialog = false },
            title = { Text("Buat Arsip ZIP", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = archiveNameInput,
                    onValueChange = { archiveNameInput = it },
                    label = { Text("Nama Arsip (.zip)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (archiveNameInput.isNotBlank()) {
                            viewModel.compressSelectedToZip(archiveNameInput.trim())
                            showCompressDialog = false
                        }
                    }
                ) {
                    Text("Kompres")
                }
            },
            dismissButton = {
                Button(onClick = { showCompressDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Action Bottom Sheet
    if (showActionSheet && selectedItemForAction != null) {
        val item = selectedItemForAction!!
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
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                DropdownMenuItem(
                    text = { Text("Buka File") },
                    onClick = { showActionSheet = false; onOpenFile(item) }
                )
                DropdownMenuItem(
                    text = { Text("Detail File") },
                    onClick = { showActionSheet = false; onFileDetailClick(item) }
                )
                DropdownMenuItem(
                    text = { Text("Bagikan") },
                    onClick = { showActionSheet = false; onShareFile(item) }
                )
                DropdownMenuItem(
                    text = { Text("Kompres ke ZIP") },
                    onClick = {
                        showActionSheet = false
                        viewModel.toggleFileSelection(item)
                        showCompressDialog = true
                        archiveNameInput = item.name.substringBeforeLast(".")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Ganti Nama") },
                    onClick = {
                        showActionSheet = false
                        newNameInput = item.name
                        showRenameDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Hapus", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showActionSheet = false
                        showDeleteConfirmDialog = true
                    }
                )
            }
        }
    }
}
