package com.elementary.tasks.core.event_tree;

import android.support.annotation.Nullable;

import java.util.List;

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

public interface TreeInterface {

    void buildTree(Param params, int position);

    boolean isEmpty();

    /**
     * Holder size of current node.
     *
     * @return number of nodes.
     */
    int size();

    /**
     * Find all nodes in a tree.
     *
     * @return list of nodes.
     */
    List<Integer> getAll();

    @Nullable
    List<Integer> getNodes(Param params);

    void remove(Param params, int position);

    void print();
}
