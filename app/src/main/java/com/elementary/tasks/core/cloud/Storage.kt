package com.elementary.tasks.core.cloud

class Storage<T>(backupable: Backupable<T>, indexable: Indexable): Backupable<T> by backupable, Indexable by indexable