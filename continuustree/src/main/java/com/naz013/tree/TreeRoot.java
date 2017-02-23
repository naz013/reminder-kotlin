package com.naz013.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeRoot<I, K, V extends TreeObject<I, K>> implements TreeInterface<I, K, V> {

    private Element<I, K> root;
    private Map<I, V> objects = new HashMap<>();

    public TreeRoot(ElementFactory<I, K> factory) {
        root = factory.getElement(0, null, null, false);
    }

    @Override
    public void add(V v) {
        K[] keys = v.getKeys();
        if (keys == null || keys.length == 0 || v.getUniqueId() == null) {
            throw new IllegalArgumentException("Object has empty items parameter");
        }
        if (root != null) {
            root.add(keys, v.getUniqueId());
        }
        if (!objects.containsKey(v.getUniqueId())) {
            objects.put(v.getUniqueId(), v);
        }
    }

    @Override
    public void remove(V v) {
        if (v.getKeys() == null || v.getUniqueId() == null) {
            throw new IllegalArgumentException("Object has empty parameter");
        }
        remove(v.getUniqueId());
    }

    @Override
    public void remove(K[] k) {
        if (k == null || k.length == 0) {
            throw new IllegalArgumentException("Empty key parameter");
        }
        if (root != null) {
            root.remove(k);
        }
    }

    @Override
    public void remove(I i) {
        if (i == null) {
            throw new IllegalArgumentException("Object has empty id parameter");
        }
        if (objects.containsKey(i)) {
            V v = objects.get(i);
            if (root != null) {
                root.remove(v.getKeys(), i);
            }
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
        if (k == null || k.length == 0) {
            throw new IllegalArgumentException("Empty key parameter");
        }
        List<V> list = new ArrayList<>();
        if (root != null) {
            for (I element : root.get(k)) {
                list.add(objects.get(element));
            }
        }
        return list;
    }

    @Override
    public int size() {
        return objects.size();
    }

    @Override
    public void clear() {
        objects.clear();
        root = null;
    }
}
