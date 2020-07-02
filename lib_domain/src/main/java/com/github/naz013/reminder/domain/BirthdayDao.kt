package com.github.naz013.reminder.domain

import com.github.naz013.reminder.data.BirthdayEntity

class BirthdayDao(
        
) : StorageDao<BirthdayEntity, String> {

    override suspend fun save(t: BirthdayEntity) {

    }

    override suspend fun get(id: String): BirthdayEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: String): Boolean {
        TODO("Not yet implemented")
    }
}