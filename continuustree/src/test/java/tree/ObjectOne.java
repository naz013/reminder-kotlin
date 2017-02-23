package tree;

import com.naz013.tree.TreeObject;

import java.util.Calendar;
import java.util.UUID;

/**
 * Copyright 2017 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class ObjectOne implements TreeObject<String, Integer> {

    private String summary = null;
    private String uuId = null;
    private long time;

    public ObjectOne(String summary, long time) {
        this.summary = summary;
        this.time = time;
        this.uuId = UUID.randomUUID().toString();
    }

    public String getSummary() {
        return summary;
    }

    @Override
    public String getUniqueId() {
        return uuId;
    }

    @Override
    public Integer[] getKeys() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return new Integer[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)};
    }
}
