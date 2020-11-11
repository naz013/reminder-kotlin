package com.elementary.tasks.core.cloud.storages

abstract class Storage : Backupable, Indexable {
  override fun toString(): String {
    return this.javaClass.name
  }
}