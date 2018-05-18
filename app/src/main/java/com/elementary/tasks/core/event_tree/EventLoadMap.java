package com.elementary.tasks.core.event_tree;

import androidx.annotation.NonNull;

import com.elementary.tasks.core.utils.TimeUtil;

import java.util.TimeZone;

import hirondelle.date4j.DateTime;

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

public class EventLoadMap {

    private Node root;

    public boolean isEmpty() {
        return this.root == null;
    }

    @NonNull
    public DateTime findAvailableSpace(@NonNull DateTime start, long duration) {
        if (isEmpty()) return start;
        if (hasSpace(start, duration)) return start;
        TimeZone timeZone = TimeZone.getTimeZone(TimeUtil.GMT);
        Node nearest = root.getNearest(start);
        int diff = start.compareTo(nearest.end);
        if (diff > 0) {
            if (nearest.higher != null) {
                if (nearest.higher.start.getMilliseconds(timeZone) - start.getMilliseconds(timeZone) > duration) {
                    return start;
                } else {
                    return findAvailableSpace(nearest.higher.end, duration);
                }
            } else {
                if (!nearest.hasParent()) {
                    return nearest.end;
                } else {
                    while (nearest.hasParent()) {
                        if (nearest.start.gt(start) && nearest.start.getMilliseconds(timeZone) - start.getMilliseconds(timeZone) > duration) {
                            return start;
                        }
                        nearest = nearest.parent;
                    }
                    return findAvailableSpace(nearest.end, duration);
                }
            }
        } else if (diff < 0) {
            if (nearest.start.getMilliseconds(timeZone) - start.getMilliseconds(timeZone) > duration) {
                return start;
            } else {
                return findAvailableSpace(nearest.end, duration);
            }
        } else {
            if (nearest.higher != null) {
                if (nearest.higher.start.getMilliseconds(timeZone) - start.getMilliseconds(timeZone) > duration) {
                    return start;
                } else {
                    return findAvailableSpace(nearest.higher.end, duration);
                }
            } else {
                if (!nearest.hasParent()) {
                    return nearest.end;
                } else {
                    while (nearest.hasParent()) {
                        if (nearest.start.gt(start) && nearest.start.getMilliseconds(timeZone) - start.getMilliseconds(timeZone) > duration) {
                            return start;
                        }
                        nearest = nearest.parent;
                    }
                    return findAvailableSpace(nearest.end, duration);
                }
            }
        }
    }

    public boolean hasSpace(@NonNull DateTime start, long duration) {
        return isEmpty() || root.hasSpace(start, duration);
    }

    public void put(@NonNull String key, @NonNull DateTime start, @NonNull DateTime end) {
        if (this.root == null) {
            this.root = new Node(key, start, end, null);
        } else {
            this.root.add(key, start, end);
        }
    }

    public void remove(@NonNull String key) {
        if (root != null) {
            root.remove(key, () -> root = null);
        }
    }

    public void clear() {
        this.root = null;
    }
}
