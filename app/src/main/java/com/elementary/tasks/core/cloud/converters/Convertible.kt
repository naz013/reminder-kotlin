package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.storages.FileIndex

interface Convertible<T> {
    /**
     * Convert object to json.
     */
    fun convert(t: T): FileIndex?

    /**
     * Convert from json to object.
     */
    fun convert(encrypted: String): T?

    fun metadata(t: T): Metadata
}