package com.elementary.tasks.core.data.models

import java.io.File

data class ShareFile<T>(val item: T, val file: File?)
