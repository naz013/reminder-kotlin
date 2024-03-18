package com.elementary.tasks.core.cloud.storages

abstract class Storage : Backupable {
  override fun toString(): String {
    return this.javaClass.name
  }
}
