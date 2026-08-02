package com.github.naz013.tags.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.naz013.tags.db.dao.TagAssignmentDao
import com.github.naz013.tags.db.dao.TagDao
import com.github.naz013.tags.db.entity.TagAssignmentEntity
import com.github.naz013.tags.db.entity.TagEntity

@Database(
  entities = [TagEntity::class, TagAssignmentEntity::class],
  version = 1,
  exportSchema = false
)
internal abstract class TagsDb : RoomDatabase() {
  abstract fun tagDao(): TagDao
  abstract fun tagAssignmentDao(): TagAssignmentDao

  companion object {
    private const val DB_NAME = "tags_db"

    @Volatile
    private var instance: TagsDb? = null

    fun getInstance(context: Context): TagsDb =
      instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(context.applicationContext, TagsDb::class.java, DB_NAME)
          .build()
          .also { instance = it }
      }
  }
}
