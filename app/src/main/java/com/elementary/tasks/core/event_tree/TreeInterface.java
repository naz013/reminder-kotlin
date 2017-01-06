package com.elementary.tasks.core.event_tree;

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

    /**
     * Add new node to tree.
     *
     * @param object node object;
     */
    void addNode(Object object);

    /**
     * Get size of current node.
     *
     * @return number of nodes.
     */
    int size();

    /**
     * Find nodes by parameters.
     *
     * @param params [0] year (if (-1) - get all nodes from tree);
     * @param params [1] month (if (-1) - get all nodes from selected year);
     * @param params [2] day (if (-1) - get all nodes from selected month);
     * @param params [3] hour (if (-1) - get all nodes from selected day);
     * @param params [4] minute (if (-1) - get all nodes from selected hour);
     * @return list of nodes.
     */
    List<Object> getNodes(int... params);

    /**
     * Find all nodes in a tree.
     *
     * @return list of nodes.
     */
    List<Object> getAll();

    /**
     * Remove all nodes from with selected key.
     *
     * @param uuId key of nodes.
     */
    void remove(String uuId);
}
