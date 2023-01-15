package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.SmsTemplate
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface SmsTemplatesDao {

    @Query("SELECT * FROM SmsTemplate")
    fun getAll(): List<SmsTemplate>

    @Query("SELECT * FROM SmsTemplate WHERE LOWER(title) LIKE '%' || :query || '%'")
    fun searchByTitle(query: String): List<SmsTemplate>

    @Query("SELECT * FROM SmsTemplate")
    fun loadAll(): LiveData<List<SmsTemplate>>

    @Insert(onConflict = REPLACE)
    fun insert(smsTemplate: SmsTemplate)

    @Insert(onConflict = REPLACE)
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
