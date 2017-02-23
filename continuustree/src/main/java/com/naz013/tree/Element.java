package com.naz013.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

public class Element<I, K> implements ElementInterface<I, K>{

    private Element<I, K> parent;
    private int maxNodes;
    private boolean isLast;
    private int keyLevel;
    private K value;
    private TreeMap<K, Element<I, K>> elements = new TreeMap<>();
    private Set<I> nodes = new TreeSet<>();
    private ElementFactory<I, K> factory;

    public Element(ElementFactory<I, K> factory, Element<I, K> parent, int keyLevel, K value) {
        this.factory = factory;
        this.parent = parent;
        this.keyLevel = keyLevel;
        this.value = value;
        this.maxNodes = Integer.MAX_VALUE;
    }

    public K getValue() {
        return value;
    }

    public Element<I, K> getParent() {
        return parent;
    }

    public void setMax(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    public void setLast(boolean last) {
        this.isLast = last;
    }

    @Override
    public void add(K[] keys, I i) {
        System.out.println("DEB " + isLast + ", Value " + value + ", Lev " + keyLevel);
        if (this.isLast) {
            this.nodes.add(i);
        } else {
            K key = keys[keyLevel];
            if (this.elements.containsKey(key)) {
                this.elements.get(key).add(keys, i);
            } else {
                boolean isLast = keys.length == keyLevel + 1;
                Element<I, K> element = factory.getElement(keyLevel + 1, key, this, isLast);
                element.setLast(isLast);
                element.add(keys, i);
                this.elements.put(key, element);
            }
        }
    }

    @Override
    public void remove(K[] k) {
        if (this.isLast) {
            nodes.clear();
        } else {
            if (k == null) {
                throw new IllegalArgumentException("Object has empty keys");
            }
            for (Element<I, K> element : elements.values()) {
                element.remove(k);
            }
        }
    }

    @Override
    public void remove(K[] k, I i) {
        if (i == null) {
            throw new NullPointerException();
        }
        if (this.isLast) {
            nodes.remove(i);
        } else {
            if (k == null) {
                throw new IllegalArgumentException("Object has empty keys");
            }
            for (Element<I, K> element : elements.values()) {
                element.remove(k, i);
            }
        }
    }

    @Override
    public List<I> get(K[] k) {
        if (this.isLast) {
            return new ArrayList<>(nodes);
        } else {
            System.out.println("DEB " + Arrays.asList(k) + ", Level " + keyLevel);
            if (keyLevel >= k.length - 1) {
                return getAll();
            }
            List<I> list = new ArrayList<>();
            if (elements.containsKey(k[keyLevel])) {
                list.addAll(elements.get(k[keyLevel]).get(k));
            }
            return list;
        }
    }

    @Override
    public List<I> getAll() {
        System.out.println("Get all " + value + ", Level " + keyLevel);
        if (this.isLast) {
            return new ArrayList<>(nodes);
        }
        List<I> list = new ArrayList<>();
        for (Element<I, K> element : elements.values()) {
            list.addAll(element.getAll());
        }
        return list;
    }

    @Override
    public int size() {
        if (isLast) return nodes.size();
        else return elements.size();
    }
}
