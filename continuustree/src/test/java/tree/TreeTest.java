package tree;

import com.naz013.tree.Element;
import com.naz013.tree.ElementFactory;
import com.naz013.tree.TreeObject;
import com.naz013.tree.TreeRoot;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;
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

public class TreeTest {

    private static TreeRoot<String, Integer, TreeObject<String, Integer>> treeRoot;
    private static ObjectOne objectOne;
    private static Calendar calendar = Calendar.getInstance();

    @BeforeClass
    public static void setup() {
        calendar.set(2017, 1, 25, 16, 30);
        objectOne = new ObjectOne("objectOne", calendar.getTimeInMillis());
        treeRoot = new TreeRoot<>(new ElementFactory<String, Integer>() {
            @Override
            public Element<String, Integer> getElement(int level, Integer value, Element<String, Integer> parent, boolean isLast) {
                Element<String, Integer> element = new Element<>(this, parent, level, value);
                if (level == 1) {
                    element.setMax(12);
                } else if (level == 2) {
                    int month = parent.getValue();
                    System.out.println("Parent value " + month);
                    int year = 2017;
                    if (parent.getParent() != null) {
                        year = parent.getParent().getValue();
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, 15);
                    int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    element.setMax(max);
                } else if (level == 3) {
                    element.setMax(24);
                } else if (level == 4) {
                    element.setMax(60);
                }
                return element;
            }
        });
    }

    @Test
    public void addItem() throws Exception {
        treeRoot.add(objectOne);
        System.out.println("Test write ID " + objectOne.getUniqueId());
        calendar.set(2017, 2, 25, 16, 30);
        treeRoot.add(new ObjectTwo("objectTwo", System.currentTimeMillis()));
        System.out.println("TREE SIZE " + treeRoot.size());
        assert treeRoot.size() == 2;
    }

    @Test
    public void getItem() throws Exception {
        System.out.println("Test read ID " + objectOne.getUniqueId());
        TreeObject<String, Integer> object = treeRoot.get(objectOne.getUniqueId());
        assert object != null;
    }

    @Test
    public void getByYear() throws Exception {
        List<TreeObject<String, Integer>> objects = treeRoot.get(new Integer[]{2017});
        System.out.println("Result " + objects.size());
        assert objects.size() == 2;
    }

    @Test
    public void getByMonth() throws Exception {
        List<TreeObject<String, Integer>> objects = treeRoot.get(new Integer[]{2017, 1});
        System.out.println("Result " + objects.size());
        assert objects.size() == 1;
    }
}
