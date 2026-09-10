package com.github.naz013.localbackup.transfer

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** The "Transfer to PRO/Free" send screen - see [TransferReceiverActivity] for the receiving side,
 * which is a separate cross-app Activity rather than a destination in this app's own nav graph. */
@Serializable
data object TransferNavKey : NavKey
