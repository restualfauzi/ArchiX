package com.archix.app.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.archix.app.data.model.FileItem
import com.archix.app.ui.screens.AboutScreen
import com.archix.app.ui.screens.ArchiveDetailScreen
import com.archix.app.ui.screens.FileDetailScreen
import com.archix.app.ui.screens.MainContainerScreen
import com.archix.app.ui.screens.SearchScreen
import com.archix.app.ui.screens.SettingsScreen
import com.archix.app.ui.screens.SplashScreen
import com.archix.app.ui.viewmodel.ArchiveViewModel
import com.archix.app.ui.viewmodel.FileManagerViewModel
import com.archix.app.ui.viewmodel.HomeViewModel
import com.archix.app.ui.viewmodel.MoreViewModel
import com.archix.app.ui.viewmodel.SearchViewModel
import com.archix.app.ui.viewmodel.SettingsViewModel
import java.io.File

object NavRoutes {
    const val SPLASH = "splash"
    const val MAIN = "main"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val FILE_DETAIL = "file_detail"
    const val ARCHIVE_DETAIL = "archive_detail"
}

@Composable
fun AppNavigation(
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    val homeViewModel: HomeViewModel = viewModel()
    val fileManagerViewModel: FileManagerViewModel = viewModel()
    val archiveViewModel: ArchiveViewModel = viewModel()
    val moreViewModel: MoreViewModel = viewModel()
    val searchViewModel: SearchViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    var activeDetailFile by remember { mutableStateOf<FileItem?>(null) }
    var activeArchiveFile by remember { mutableStateOf<FileItem?>(null) }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.MAIN) {
            MainContainerScreen(
                homeViewModel = homeViewModel,
                fileManagerViewModel = fileManagerViewModel,
                archiveViewModel = archiveViewModel,
                moreViewModel = moreViewModel,
                hasStoragePermission = hasStoragePermission,
                onRequestPermission = onRequestPermission,
                onSearchClick = { navController.navigate(NavRoutes.SEARCH) },
                onSettingsClick = { navController.navigate(NavRoutes.SETTINGS) },
                onAboutClick = { navController.navigate(NavRoutes.ABOUT) },
                onFileDetailClick = { item ->
                    activeDetailFile = item
                    navController.navigate(NavRoutes.FILE_DETAIL)
                },
                onViewArchiveDetails = { archive ->
                    activeArchiveFile = archive
                    navController.navigate(NavRoutes.ARCHIVE_DETAIL)
                },
                onOpenFile = { item ->
                    openFileWithIntent(context, item)
                },
                onShareFile = { item ->
                    shareFileWithIntent(context, item)
                }
            )
        }

        composable(NavRoutes.SEARCH) {
            SearchScreen(
                viewModel = searchViewModel,
                onBackClick = { navController.popBackStack() },
                onFileClick = { item ->
                    if (item.isDirectory) {
                        fileManagerViewModel.navigateToFolder(item.path)
                        navController.popBackStack()
                    } else {
                        openFileWithIntent(context, item)
                    }
                },
                onFileMoreClick = { item ->
                    activeDetailFile = item
                    navController.navigate(NavRoutes.FILE_DETAIL)
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ABOUT) {
            AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.FILE_DETAIL) {
            activeDetailFile?.let { item ->
                FileDetailScreen(
                    fileItem = item,
                    onBackClick = { navController.popBackStack() },
                    onOpenFile = { openFileWithIntent(context, it) },
                    onShareFile = { shareFileWithIntent(context, it) },
                    onDeleteFile = {
                        fileManagerViewModel.deleteFile(it)
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(NavRoutes.ARCHIVE_DETAIL) {
            activeArchiveFile?.let { archive ->
                ArchiveDetailScreen(
                    archiveItem = archive,
                    viewModel = archiveViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun openFileWithIntent(context: Context, fileItem: FileItem) {
    try {
        val file = File(fileItem.path)
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeType(fileItem.extension))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Buka file dengan..."))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun shareFileWithIntent(context: Context, fileItem: FileItem) {
    try {
        val file = File(fileItem.path)
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            type = getMimeType(fileItem.extension)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan file via..."))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun getMimeType(extension: String): String {
    return when (extension.lowercase()) {
        "jpg", "jpeg", "png", "gif", "webp" -> "image/*"
        "mp4", "mkv", "avi", "mov" -> "video/*"
        "mp3", "wav", "flac", "m4a" -> "audio/*"
        "pdf" -> "application/pdf"
        "txt", "rtf" -> "text/plain"
        "doc", "docx" -> "application/msword"
        "zip" -> "application/zip"
        "rar" -> "application/x-rar-compressed"
        "7z" -> "application/x-7z-compressed"
        "apk" -> "application/vnd.android.package-archive"
        else -> "*/*"
    }
}
