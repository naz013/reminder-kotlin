package com.github.naz013.sync

/**
 * Sync API interface.
 *
 * The sync is done in the next way:
 * 1. Upload local changes to the cloud (sync states: Waiting for Upload or Failed to Upload).
 * 2. Download the list of files.
 * 3. For each file in the list, check if its last modified date is newer than in the metadata database.:
 *  - If yes, download the file and update the local database.
 *  - If no, skip the file.
 * 4. Update the sync states in the local database.
 * 5. Handle conflicts if any (e.g., both local and cloud versions have changes).
 * 6. Finalize the sync process and notify the user if necessary.
 */
interface SyncApi {

  /**
   * Sync all data types.
   */
  @Throws(Exception::class)
  suspend fun sync()

  /**
   * Sync specific data type.
   */
  @Throws(Exception::class)
  suspend fun sync(dataType: DataType)

  /**
   * Sync specific id of a data type.
   */
  @Throws(Exception::class)
  suspend fun sync(dataType: DataType, id: String)

  /**
   * Upload all data types.
   */
  @Throws(Exception::class)
  suspend fun upload()

  /**
   * Upload specific ids of a data type.
   */
  @Throws(Exception::class)
  suspend fun upload(dataType: DataType, ids: List<String>)

  /**
   * Upload specific id of a data type.
   */
  @Throws(Exception::class)
  suspend fun upload(dataType: DataType, id: String)
}
