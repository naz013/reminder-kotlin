package com.elementary.tasks.core.data.models

import com.elementary.tasks.core.utils.SuperUtil
import com.google.api.client.util.DateTime
import com.google.api.services.tasks.model.Task
import com.google.gson.annotations.SerializedName

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Entity
class GoogleTask {

    @SerializedName("title")
    var title: String? = null
    @SerializedName("taskId")
    @PrimaryKey
    var taskId: String? = null
    @SerializedName("completeDate")
    var completeDate: Long = 0
    @SerializedName("del")
    var del: Int = 0
    @SerializedName("dueDate")
    var dueDate: Long = 0
    @SerializedName("eTag")
    private var eTag: String? = null
    @SerializedName("kind")
    var kind: String? = null
    @SerializedName("notes")
    var notes: String? = null
    @SerializedName("parent")
    var parent: String? = null
    @SerializedName("position")
    var position: String? = null
    @SerializedName("selfLink")
    var selfLink: String? = null
    @SerializedName("updateDate")
    var updateDate: Long = 0
    @SerializedName("listId")
    private var listId: String? = null
    @SerializedName("status")
    var status: String? = null
    @SerializedName("uuId")
    var uuId: String? = null
    @SerializedName("hidden")
    var hidden: Int = 0

    constructor() {}

    @Ignore
    constructor(item: GoogleTask) {
        this.listId = item.getListId()
        this.selfLink = item.selfLink
        this.kind = item.kind
        this.eTag = item.geteTag()
        this.title = item.title
        this.taskId = item.taskId
        this.completeDate = item.completeDate
        this.del = item.del
        this.hidden = item.hidden
        this.dueDate = item.dueDate
        this.notes = item.notes
        this.parent = item.parent
        this.position = item.position
        this.updateDate = item.updateDate
        this.status = item.status
        this.uuId = item.uuId
    }

    @Ignore
    constructor(task: Task, listId: String) {
        this.listId = listId
        val dueDate = task.due
        val due = dueDate?.value ?: 0
        val completeDate = task.completed
        val complete = completeDate?.value ?: 0
        val updateDate = task.updated
        val update = updateDate?.value ?: 0
        val taskId = task.id
        var isDeleted = false
        try {
            isDeleted = task.deleted!!
        } catch (ignored: NullPointerException) {
        }

        var isHidden = false
        try {
            isHidden = task.hidden!!
        } catch (ignored: NullPointerException) {
        }

        this.selfLink = task.selfLink
        this.kind = task.kind
        this.eTag = task.etag
        this.title = task.title
        this.taskId = taskId
        this.completeDate = complete
        this.del = if (isDeleted) 1 else 0
        this.hidden = if (isHidden) 1 else 0
        this.dueDate = due
        this.notes = task.notes
        this.parent = task.parent
        this.position = task.position
        this.updateDate = update
        this.status = task.status
    }

    fun update(task: Task) {
        val dueDate = task.due
        val due = dueDate?.value ?: 0
        val completeDate = task.completed
        val complete = completeDate?.value ?: 0
        val updateDate = task.updated
        val update = updateDate?.value ?: 0
        var isDeleted = false
        try {
            isDeleted = task.deleted!!
        } catch (ignored: NullPointerException) {
        }

        var isHidden = false
        try {
            isHidden = task.hidden!!
        } catch (ignored: NullPointerException) {
        }

        this.selfLink = task.selfLink
        this.kind = task.kind
        this.eTag = task.etag
        this.title = task.title
        this.taskId = task.id
        this.completeDate = complete
        this.del = if (isDeleted) 1 else 0
        this.hidden = if (isHidden) 1 else 0
        this.dueDate = due
        this.notes = task.notes
        this.parent = task.parent
        this.position = task.position
        this.updateDate = update
        this.status = task.status
    }

    fun geteTag(): String? {
        return eTag
    }

    fun seteTag(eTag: String) {
        this.eTag = eTag
    }

    fun getListId(): String? {
        return listId
    }

    fun setListId(listId: String): GoogleTask {
        this.listId = listId
        return this
    }

    override fun toString(): String {
        return SuperUtil.getObjectPrint(this, GoogleTask::class.java)
    }
}
