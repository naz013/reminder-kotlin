package com.elementary.tasks.core.event_tree;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.elementary.tasks.core.utils.TimeUtil;
import com.google.gson.internal.LinkedHashTreeMap;

import java.util.Map;
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

public class Node {

    private String key;
    DateTime start;
    DateTime end;
    Node parent;
    private Node lower;
    Node higher;

    private Map<String, Node> nodeMap = new LinkedHashTreeMap<>();

    Node(@NonNull String key, @NonNull DateTime start, @NonNull DateTime end, @Nullable Node parent) {
        this.start = start;
        this.parent = parent;
        this.end = end;
        this.key = key;
    }

    boolean isParent() {
        return !hasParent();
    }

    boolean hasParent() {
        return this.parent != null;
    }

    private boolean hasLower() {
        return this.lower != null;
    }

    private boolean hasHigher() {
        return this.higher != null;
    }

    @NonNull
    Node getNearest(@NonNull DateTime anchor) {
        int diff = anchor.compareTo(this.end);
        if (diff > 0) {
            if (hasHigher()) {
                return this.higher.getNearest(anchor);
            }
        } else if (diff < 0) {
            if (hasLower()) {
                return this.lower.getNearest(anchor);
            }
        }
        return this;
    }

    boolean hasSpace(@NonNull DateTime start, long duration) {
        int diff = start.compareTo(this.end);
        TimeZone timeZone = TimeZone.getTimeZone(TimeUtil.GMT);
        if (diff > 0) {
            if (hasHigher()) {
                return this.higher.hasSpace(start, duration);
            } else {
                Node toCompare = this;
                do {
                    if (toCompare.start.gt(start)) {
                        return toCompare.start.getMilliseconds(timeZone) - start.getMilliseconds(timeZone) > duration;
                    }
                    toCompare = toCompare.parent;
                } while (toCompare.hasParent());
                return true;
            }
        } else if (diff < 0) {
            if (hasLower()) {
                return this.lower.hasSpace(start, duration);
            } else {
                return this.start.getMilliseconds(timeZone) - start.getMilliseconds(timeZone) > duration;
            }
        } else {
            if (hasHigher()) {
                return this.higher.start.getMilliseconds(timeZone) - start.getMilliseconds(timeZone) > duration;
            } else {
                Node toCompare = this;
                do {
                    if (toCompare.start.gt(start)) {
                        return toCompare.start.getMilliseconds(timeZone) - start.getMilliseconds(timeZone) > duration;
                    }
                    toCompare = toCompare.parent;
                } while (toCompare.hasParent());
                return true;
            }
        }
    }

    public void add(@NonNull String key, @NonNull DateTime start, @NonNull DateTime end) {
        int diff = start.compareTo(this.start);
        if (diff == 0) {
            this.nodeMap.put(key, new Node(key, start, end, this.parent));
        } else if (diff < 0) {
            if (hasLower()) {
                this.lower.add(key, start, end);
            } else {
                this.lower = new Node(key, start, end, this);
            }
        } else if (diff > 0){
            if (hasHigher()) {
                this.higher.add(key, start, end);
            } else {
                this.higher = new Node(key, start, end, this);
            }
        }
    }

    public void add(@NonNull Node node) {
        int diff = node.start.compareTo(this.start);
        if (diff == 0) {
            node.parent = this.parent;
            this.nodeMap.put(key, node);
        } else if (diff < 0) {
            if (hasLower()) {
                this.lower.add(node);
            } else {
                node.parent = this;
                this.lower = node;
            }
        } else if (diff > 0){
            if (hasHigher()) {
                this.higher.add(node);
            } else {
                node.parent = this;
                this.higher = node;
            }
        }
    }

    public void remove(@NonNull String key, @NonNull NullValidator validator) {
        if (this.key.equals(key)) {
            if (this.nodeMap.isEmpty()) {
                Node n = null;
                if (hasHigher() && hasLower()) {
                    n = this.lower;
                    n.add(this.higher);
                } else if (hasHigher()) {
                    n = this.higher;
                } else if (hasLower()) {
                    n = this.lower;
                }
                if (n != null && hasParent()) {
                    n.parent = this.parent;
                    if (this.parent.lower.equals(this)) {
                        this.parent.lower = n;
                    } else if (this.parent.higher.equals(this)) {
                        this.parent.higher = n;
                    }
                } else if (n != null){
                    n.parent = this.parent;
                    this.assignNew(n);
                } else {
                    validator.invalidate();
                }
            } else {
                String k = nodeMap.keySet().iterator().next();
                Node n = nodeMap.get(k);
                nodeMap.remove(k);
                n.parent = this.parent;
                n.lower = this.lower;
                n.higher = this.higher;
                if (hasParent()) {
                    n.parent = this.parent;
                    if (this.parent.lower.equals(this)) {
                        this.parent.lower = n;
                    } else if (this.parent.higher.equals(this)) {
                        this.parent.higher = n;
                    }
                }
            }
        } else if (this.nodeMap.containsKey(key)) {
            this.nodeMap.remove(key);
        } else {
            this.lower.remove(key, validator);
            this.higher.remove(key, validator);
        }
    }

    private void assignNew(@NonNull Node node) {
        this.start = node.start;
        this.parent = node.parent;
        this.end = node.end;
        this.key = node.key;
        this.nodeMap = node.nodeMap;
        this.higher = node.higher;
        this.lower = node.lower;
    }
}
