package com.example.cloudtestadmin

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.graphics.BitmapFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.Source
import com.github.naz013.cloudapi.dropbox.DropboxAuthManager
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager
import com.github.naz013.logging.Logger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import org.koin.compose.koinInject

/**
 * Main composable for the cloud test admin screen.
 *
 * Displays different screens based on the current UI state.
 *
 * @param viewModel The ViewModel instance to use. If not provided, will be injected via Koin.
 */
@Composable
fun CloudTestScreen(
  viewModel: CloudTestViewModel = koinInject()
) {
  val uiState by viewModel.uiState.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(errorMessage) {
    errorMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearError()
    }
  }

  // Handle back press based on current UI state
  // Only intercept back press when not on the source selection screen
  BackHandler(enabled = uiState !is CloudTestUiState.SelectSource) {
    Logger.d("CloudTestScreen", "Back press detected, current state: ${uiState::class.simpleName}")
    when (val state = uiState) {
      is CloudTestUiState.SelectSource -> {
        // This should never be called due to enabled condition
        Logger.d("CloudTestScreen", "On source selection screen, allowing system back press")
      }
      is CloudTestUiState.NeedAuth -> {
        Logger.d("CloudTestScreen", "Back from auth screen to source selection")
        viewModel.backToSourceSelection()
      }
      is CloudTestUiState.FolderList -> {
        Logger.d("CloudTestScreen", "Back from folder list to source selection")
        viewModel.backToSourceSelection()
      }
      is CloudTestUiState.FileList -> {
        Logger.d("CloudTestScreen", "Back from file list to folder list")
        viewModel.backToFolderList()
      }
      is CloudTestUiState.FilePreview -> {
        Logger.d("CloudTestScreen", "Back from file preview to file list")
        viewModel.backToFileList(state.dataType)
      }
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (val state = uiState) {
        is CloudTestUiState.SelectSource -> {
          SourceSelectionScreen(
            onSourceSelected = { source -> viewModel.selectSource(source) },
            isAuthenticated = { source -> viewModel.isAuthenticated(source) }
          )
        }
        is CloudTestUiState.NeedAuth -> {
          AuthenticationScreen(
            source = state.source,
            onAuthComplete = { viewModel.onAuthenticationComplete() },
            onBackPressed = { viewModel.backToSourceSelection() }
          )
        }
        is CloudTestUiState.FolderList -> {
          FolderListScreen(
            dataTypes = state.dataTypes,
            onFolderSelected = { dataType -> viewModel.loadFiles(dataType) },
            onBackPressed = { viewModel.backToSourceSelection() },
            onLogout = { viewModel.logout() }
          )
        }
        is CloudTestUiState.FileList -> {
          FileListScreen(
            viewModel = viewModel,
            dataType = state.dataType,
            files = state.files,
            onFileSelected = { file -> viewModel.previewFile(state.dataType, file) },
            onBackPressed = { viewModel.backToFolderList() }
          )
        }
        is CloudTestUiState.FilePreview -> {
          FilePreviewScreen(
            dataType = state.dataType,
            cloudFile = state.cloudFile,
            content = state.content,
            imageData = state.imageData,
            onBackPressed = { viewModel.backToFileList(state.dataType) }
          )
        }
      }

      if (isLoading) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      }
    }
  }
}

/**
 * Screen for selecting a cloud source (Google Drive or Dropbox).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceSelectionScreen(
  onSourceSelected: (Source) -> Unit,
  isAuthenticated: (Source) -> Boolean
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Select Cloud Service") }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "Choose a cloud service to manage files",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      SourceCard(
        title = "Google Drive",
        subtitle = if (isAuthenticated(Source.GoogleDrive)) "Connected" else "Not connected",
        isAuthenticated = isAuthenticated(Source.GoogleDrive),
        onClick = { onSourceSelected(Source.GoogleDrive) }
      )

      SourceCard(
        title = "Dropbox",
        subtitle = if (isAuthenticated(Source.Dropbox)) "Connected" else "Not connected",
        isAuthenticated = isAuthenticated(Source.Dropbox),
        onClick = { onSourceSelected(Source.Dropbox) }
      )
    }
  }
}

/**
 * Card component for displaying a cloud source option.
 */
@Composable
fun SourceCard(
  title: String,
  subtitle: String,
  isAuthenticated: Boolean,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = if (isAuthenticated) Icons.Default.CloudUpload else Icons.Default.Cloud,
        contentDescription = null,
        tint = if (isAuthenticated) {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        }
      )
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(start = 16.dp)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = if (isAuthenticated) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          }
        )
      }
    }
  }
}

/**
 * Screen for handling authentication for a cloud source.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(
  source: Source,
  onAuthComplete: () -> Unit,
  onBackPressed: () -> Unit
) {
  val context = LocalContext.current

  when (source) {
    Source.GoogleDrive -> {
      val googleDriveAuthManager: GoogleDriveAuthManager = koinInject()
      val googleDriveApi: com.github.naz013.cloudapi.googledrive.GoogleDriveApi = koinInject()
      val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
      ) { result ->
       Logger.d("CloudTestScreen", "Google Sign-In result received, resultCode=${result.resultCode}")

        try {
          val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
          val account = task.getResult(Exception::class.java)

          if (account != null) {
            val email = account.email
            Logger.i("CloudTestScreen", "Google Sign-In successful, email=$email")

            if (email != null) {
              // Save the user name to storage
              googleDriveAuthManager.saveUserName(email)
              Logger.d("CloudTestScreen", "User name saved to storage")

              // Re-initialize the Google Drive API
              googleDriveApi.initialize()
              Logger.d("CloudTestScreen", "Google Drive API initialized")

              // Complete authentication
              onAuthComplete()
            } else {
              Logger.e("CloudTestScreen", "Email is null after sign-in")
              onAuthComplete()
            }
          } else {
            Logger.e("CloudTestScreen", "Account is null after sign-in")
            onAuthComplete()
          }
        } catch (e: Exception) {
          Logger.e("CloudTestScreen", "Error processing Google Sign-In result: ${e.message}", e)
          onAuthComplete()
        }
      }

      Scaffold(
        topBar = {
          TopAppBar(
            title = { Text("Google Drive Login") },
            navigationIcon = {
              IconButton(onClick = onBackPressed) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            }
          )
        }
      ) { paddingValues ->
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Click the button below to sign in with Google",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
          )

          androidx.compose.material3.Button(
            onClick = {
              Logger.i("CloudTestScreen", "Starting Google Sign-In flow")

              try {
                val scopes = googleDriveAuthManager.getScopes().map { Scope(it) }
                Logger.d("CloudTestScreen", "Scopes: ${scopes.map { it.scopeUri }}")

                val firstScope = scopes.first()
                val restScopes = scopes.drop(1).toTypedArray()

                val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                  .requestScopes(firstScope, *restScopes)
                  .requestEmail()
                  .build()
                val client = GoogleSignIn.getClient(context, signInOptions)

                Logger.d("CloudTestScreen", "Launching Google Sign-In intent")
                launcher.launch(client.signInIntent)
              } catch (e: Exception) {
                Logger.e("CloudTestScreen", "Error starting Google Sign-In: ${e.message}", e)
              }
            }
          ) {
            Text("Sign in with Google")
          }
        }
      }
    }
    Source.Dropbox -> {
      val dropboxAuthManager: DropboxAuthManager = koinInject()

      LaunchedEffect(Unit) {
        if (!dropboxAuthManager.isAuthorized()) {
          dropboxAuthManager.startAuth()
        }
      }

      Scaffold(
        topBar = {
          TopAppBar(
            title = { Text("Dropbox Login") },
            navigationIcon = {
              IconButton(onClick = onBackPressed) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            }
          )
        }
      ) { paddingValues ->
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Authenticating with Dropbox...",
            style = MaterialTheme.typography.bodyLarge
          )
          Spacer(modifier = Modifier.height(16.dp))
          CircularProgressIndicator()
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "You will be redirected to Dropbox to authorize access",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

/**
 * Screen displaying the list of folders (DataTypes).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(
  dataTypes: List<CloudTestUiState.DataType>,
  onFolderSelected: (CloudTestUiState.DataType) -> Unit,
  onBackPressed: () -> Unit,
  onLogout: () -> Unit
) {
  var showClearAllDialog by remember { mutableStateOf(false) }
  var showMenu by remember { mutableStateOf(false) }
  val viewModel: CloudTestViewModel = koinInject()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Folders") },
        navigationIcon = {
          IconButton(onClick = onBackPressed) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
          }
          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("Clear All Data") },
              onClick = {
                showMenu = false
                showClearAllDialog = true
              },
              leadingIcon = {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
              }
            )
            DropdownMenuItem(
              text = { Text("Logout") },
              onClick = {
                showMenu = false
                onLogout()
              },
              leadingIcon = {
                Icon(Icons.Default.Logout, contentDescription = null)
              }
            )
          }
        }
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(dataTypes) { dataType ->
        FolderItem(
          dataType = dataType,
          onClick = { onFolderSelected(dataType) }
        )
      }
    }

    // Clear All Data confirmation dialog
    if (showClearAllDialog) {
      AlertDialog(
        onDismissRequest = { showClearAllDialog = false },
        icon = {
          Icon(Icons.Default.DeleteForever, contentDescription = null)
        },
        title = {
          Text("Clear All Data?")
        },
        text = {
          Text("This will permanently delete ALL files from cloud storage. This action cannot be undone.")
        },
        confirmButton = {
          TextButton(
            onClick = {
              showClearAllDialog = false
              viewModel.clearAllData()
            }
          ) {
            Text("Clear All")
          }
        },
        dismissButton = {
          TextButton(onClick = { showClearAllDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }
  }
}

/**
 * Item component for displaying a folder (DataType).
 */
@Composable
fun FolderItem(
  dataType: CloudTestUiState.DataType,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Folder,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
      )
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(start = 16.dp)
      ) {
        Text(
          text = dataType.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = dataType.fileExtension,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

/**
 * Screen displaying the list of files in a folder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
  viewModel: CloudTestViewModel,
  dataType: CloudTestUiState.DataType,
  files: List<CloudFile>,
  onFileSelected: (CloudFile) -> Unit,
  onBackPressed: () -> Unit
) {
  var showDeleteAllDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("${dataType.name} Files") },
        navigationIcon = {
          IconButton(onClick = onBackPressed) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (files.isNotEmpty()) {
            IconButton(onClick = { showDeleteAllDialog = true }) {
              Icon(Icons.Default.DeleteForever, contentDescription = "Delete all files")
            }
          }
        }
      )
    }
  ) { paddingValues ->
    if (files.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "No files found",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(files) { file ->
          FileItem(
            dataType = dataType,
            file = file,
            onFileClick = { onFileSelected(file) },
            onDeleteClick = { viewModel.deleteFile(file, dataType) }
          )
        }
      }
    }

    // Delete all files confirmation dialog
    if (showDeleteAllDialog) {
      AlertDialog(
        onDismissRequest = { showDeleteAllDialog = false },
        icon = {
          Icon(Icons.Default.DeleteForever, contentDescription = null)
        },
        title = {
          Text("Delete All ${dataType.name} Files?")
        },
        text = {
          Text("This will permanently delete all ${files.size} files in this folder. This action cannot be undone.")
        },
        confirmButton = {
          TextButton(
            onClick = {
              showDeleteAllDialog = false
              viewModel.deleteAllFilesInFolder(dataType)
            }
          ) {
            Text("Delete All")
          }
        },
        dismissButton = {
          TextButton(onClick = { showDeleteAllDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }
  }
}

/**
 * Item component for displaying a file.
 */
@Composable
fun FileItem(
  dataType: CloudTestUiState.DataType,
  file: CloudFile,
  onFileClick: () -> Unit,
  onDeleteClick: () -> Unit
) {
  var showDeleteDialog by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onFileClick)
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.InsertDriveFile,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.secondary
      )
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(start = 16.dp)
      ) {
        Text(
          text = file.name,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = "Size: ${file.size} bytes",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (file.lastModified > 0) {
          Text(
            text = "Modified: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(file.lastModified))}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
      IconButton(
        onClick = { showDeleteDialog = true }
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete file",
          tint = MaterialTheme.colorScheme.error
        )
      }
    }
  }

  // Delete file confirmation dialog
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      icon = {
        Icon(Icons.Default.Delete, contentDescription = null)
      },
      title = {
        Text("Delete File?")
      },
      text = {
        Text("Are you sure you want to delete '${file.name}'? This action cannot be undone.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteDialog = false
            onDeleteClick()
          }
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

/**
 * Screen for previewing the decoded content of a file.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePreviewScreen(
  dataType: CloudTestUiState.DataType,
  cloudFile: CloudFile,
  content: String,
  imageData: ByteArray? = null,
  onBackPressed: () -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Preview: ${cloudFile.name}") },
        navigationIcon = {
          IconButton(onClick = onBackPressed) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // File info header
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "File Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Type: ${dataType.name}",
            style = MaterialTheme.typography.bodyMedium
          )
          Text(
            text = "Extension: ${dataType.fileExtension}",
            style = MaterialTheme.typography.bodyMedium
          )
          Text(
            text = "Size: ${cloudFile.size} bytes",
            style = MaterialTheme.typography.bodyMedium
          )
          if (cloudFile.lastModified > 0) {
            Text(
              text = "Modified: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(cloudFile.lastModified))}",
              style = MaterialTheme.typography.bodyMedium
            )
          }
        }
      }

      // Content preview
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 16.dp)
          .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Content Preview",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = when {
                dataType.name == "NoteImages" -> "IMAGE"
                dataType.name == "Settings" -> "XML"
                else -> "JSON"
              },
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary
            )
          }
          Spacer(modifier = Modifier.height(8.dp))

          // Display image or text content
          if (imageData != null && dataType.name == "NoteImages") {
            // Display image preview
            Box(
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
              contentAlignment = Alignment.Center
            ) {
              val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
              if (bitmap != null) {
                Image(
                  bitmap = bitmap.asImageBitmap(),
                  contentDescription = "Note image preview",
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                  contentScale = ContentScale.Fit
                )
              } else {
                Text(
                  text = "Failed to decode image",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.error
                )
              }
            }
          } else {
            // Scrollable text content area with horizontal scroll support
            Box(
              modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
            ) {
              Text(
                text = content,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }
  }
}

