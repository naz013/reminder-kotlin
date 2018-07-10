package com.elementary.tasks.backups

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

class UserItem {

    var name: String = ""
    var photo: String = ""
    var quota: Long = 0
    var used: Long = 0
    var count: Int = 0
    var kind: UserInfoAsync.Info? = null

    constructor()

    constructor(name: String, quota: Long, used: Long, count: Int, photo: String) {
        this.name = name
        this.quota = quota
        this.used = used
        this.count = count
        this.photo = photo
    }
}
