package net.forthecrown.registry;

import net.forthecrown.utils.CrownUtils;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BaseRegistry<V> implements Registry<V> {
    private final Map<Key, V> entries = new HashMap<>();

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return entries.values().iterator();
    }

    @Override
    public V get(Key key) {
        key = CrownUtils.checkNotBukkit(key);
        return entries.get(key);
    }

    @Override
    public V register(Key key, V raw) {
        key = CrownUtils.checkNotBukkit(key);
        entries.put(key, raw);
        return raw;
    }

    @Override
    public void remove(Key key) {
        key = CrownUtils.checkNotBukkit(key);
        entries.remove(key);
    }

    @Override
    public Set<Key> getKeys() {
        return entries.keySet();
    }

    @Override
    public boolean contains(Key key) {
        key = CrownUtils.checkNotBukkit(key);
        return entries.containsKey(key);
    }

    @Override
    public boolean contains(V value) {
        return entries.containsValue(value);
    }

    @Override
    public Collection<V> getEntries() {
        return entries.values();
    }
}
