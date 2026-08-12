# Cloud Test Admin

A simple Android application for testing cloud storage integration with Google Drive and Dropbox.

## Features

- **Cloud Service Selection**: Choose between Google Drive and Dropbox
- **Authentication**: Sign in with Google or Dropbox accounts
- **Folder Browser**: Browse data type folders (Reminders, Notes, Birthdays, etc.)
- **File List**: View files in each folder with metadata (name, size, last modified date)

## Architecture

### Screens

1. **Source Selection Screen**: Initial screen to select Google Drive or Dropbox
2. **Authentication Screen**: Handles OAuth flow for the selected cloud service
3. **Folder List Screen**: Displays all available DataType folders
4. **File List Screen**: Shows files in the selected folder using CloudFileApi

### Key Components

- **CloudTestViewModel**: Manages UI state and cloud operations
- **CloudTestScreen**: Main Compose UI with navigation between screens
- **CloudTestApplication**: Initializes Koin dependency injection

### Data Flow

```
Select Source → Authenticate → Browse Folders (DataType) → View Files (CloudFile)
```

## Dependencies

- **Compose**: Modern UI toolkit
- **Koin**: Dependency injection
- **Cloud API**: Google Drive and Dropbox integration
- **Sync**: DataType definitions

## Usage

1. Launch the app
2. Select a cloud service (Google Drive or Dropbox)
3. Sign in if not already authenticated
4. Browse folders representing different data types
5. Click on a folder to view files with the corresponding file extension
6. Use the back button to navigate, or logout to switch cloud services

## Cloud Services

### Google Drive
- Uses Google Sign-In API
- Requires Google Play Services
- Scopes defined in GoogleDriveAuthManager

### Dropbox
- Uses Dropbox OAuth2
- Launches browser for authentication
- Token stored via DropboxAuthManager

