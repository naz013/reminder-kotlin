package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elementary.tasks.core.data.models.SmsTemplate

@Deprecated("After S")
@Dao
interface SmsTemplatesDao {

    @Query("SELECT * FROM SmsTemplate")
    fun getAll(): List<SmsTemplate>

    @Query("SELECT * FROM SmsTemplate WHERE LOWER(title) LIKE '%' || :query || '%'")
    fun searchByTitle(query: String): List<SmsTemplate>

    @Query("SELECT * FROM SmsTemplate")
    fun loadAll(): LiveData<List<SmsTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(smsTemplate: SmsTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg smsTemplates: SmsTemplate)

    @Delete
    fun delete(smsTemplate: SmsTemplate)

    @Query("SELECT * FROM SmsTemplate WHERE `key`=:key")
    fun loadByKey(key: String): LiveData<SmsTemplate>

    @Query("SELECT * FROM SmsTemplate WHERE `key`=:key")
    fun getByKey(key: String): SmsTemplate?

    @Query("DELETE FROM SmsTemplate")
    fun deleteAll()
}
