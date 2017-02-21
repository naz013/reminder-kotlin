package com.naz013.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class TreeRoot<I, K, V extends TreeObject<I, K>> implements TreeInterface<I, K, V> {

    private TreeMap<K, Element<I, K, V>> nodes = new TreeMap<>();
    private Map<I, V> objects = new HashMap<>();

    public TreeRoot() {

    }

    @Override
    public void add(V v) {
        K[] keys = v.getKeys();
        if (keys == null || v.getUniqueId() == null) {
            throw new IllegalArgumentException("Object has empty items parameter");
        }
        K zero = keys[0];
        boolean isLast = keys.length == 1;
        if (objects.containsKey(v.getUniqueId())) {
            if (nodes.containsKey(zero)) {
                nodes.get(zero).add(v);
            } else {
                addNewNode(zero, v, isLast);
            }
        } else {
            addNewNode(zero, v, isLast);
            objects.put(v.getUniqueId(), v);
        }
    }

    private void addNewNode(K key, V v, boolean isLast) {
        Element<I, K, V> element = new Element<>(null, 1, key);
        element.setLast(isLast);
        element.add(v);
        nodes.put(key, element);
    }

    @Override
    public void remove(V v) {
        if (v.getKeys() == null || v.getUniqueId() == null) {
            throw new IllegalArgumentException("Object has empty parameter");
        }
        if (objects.containsKey(v.getUniqueId())) {
            remove(v.getUniqueId());
        } else {
            remove(v.getKeys());
        }
    }

    @Override
    public void remove(K[] k) {
        if (k == null) {
            throw new IllegalArgumentException("Empty key parameter");
        }
        for (Element<I, K, V> element : nodes.values()) {
            element.remove(k);
        }
    }

    @Override
    public void remove(I i) {
        if (i == null) {
            throw new IllegalArgumentException("Object has empty id parameter");
        }
        if (objects.containsKey(i)) {
            V v = objects.get(i);
            remove(v.getKeys());
        }
    }

    @Override
    public V get(I i) {
        if (i == null) {
            throw new IllegalArgumentException("Object has empty id parameter");
        }
        if (objects.containsKey(i)) {
            return objects.get(i);
        }
        return null;
    }

    @Override
    public boolean contains(V v) {
        if (v == null) {
            throw new NullPointerException();
        }
        return objects.containsKey(v.getUniqueId());
    }

    @Override
    public List<V> get(K[] k) {
        List<V> list = new ArrayList<>();
        for (Element<I, K, V> element : nodes.values()) {
            for (I el : element.get(k)) {
                list.add(objects.get(el));
            }
        }
        return list;
    }

    @Override
    public int size() {
        return objects.size();
    }

    private class Element<EI, EK, EV extends TreeObject<EI, EK>> implements ElementInterface<EI, EK, EV>{

        private Element<EI, EK, EV> parent;
        private int maxNodes;
        private boolean isLast;
        private int keyLevel;
        private EK value;
        private TreeMap<EK, Element<EI, EK, EV>> elements = new TreeMap<>();
        private Set<EI> nodes = new TreeSet<>();

        Element(Element<EI, EK, EV> parent, int keyLevel, EK value) {
            this.parent = parent;
            this.keyLevel = keyLevel;
            this.value = value;
            this.maxNodes = 0;
        }

        void setLast(boolean last) {
            this.isLast = last;
        }

        @Override
        public void add(EV ev) {
            if (this.isLast) {
                this.nodes.add(ev.getUniqueId());
            } else {
                EK[] keys = ev.getKeys();
                EK key = keys[keyLevel];
                if (this.elements.containsKey(key)) {
                    this.elements.get(key).add(ev);
                } else {
                    boolean isLast = keys.length == keyLevel + 1;
                    Element<EI, EK, EV> element = new Element<>(this, keyLevel + 1, key);
                    element.setLast(isLast);
                    element.add(ev);
                    this.elements.put(key, element);
                }
            }
        }

        @Override
        public void remove(EK[] ek) {
            if (this.isLast) {
                nodes.clear();
            } else {
                if (ek == null) {
                    throw new IllegalArgumentException("Object has empty keys");
                }
                for (Element<EI, EK, EV> element : elements.values()) {
                    element.remove(ek);
                }
            }
        }

        @Override
        public void remove(EV ev) {
            if (ev == null) {
                throw new NullPointerException();
            }
            remove(ev.getKeys());
        }

        @Override
        public List<EI> get(EK[] ek) {
            if (this.isLast) {
                return new ArrayList<>(nodes);
            } else {
                List<EI> list = new ArrayList<>();
                for (Element<EI, EK, EV> element : elements.values()) {
                    list.addAll(element.get(ek));
                }
                return list;
            }
        }

        @Override
        public int size() {
            if (isLast) return nodes.size();
            else return elements.size();
        }
    }
}
