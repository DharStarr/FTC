package net.forthecrown.dungeons.rewrite_4;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.dungeons.rewrite_4.component.BossStatusListener;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class CompTracker<T> implements Iterable<T> {
    private final List<T> components = new ObjectArrayList<>();

    private final Class<T> clazz;

    private DungeonBoss boss;

    public CompTracker(Class<T> clazz) {
        this.clazz = clazz;
    }

    public static CompTracker<BossStatusListener> bossStatusFamily() {
        return new CompTracker<>(BossStatusListener.class);
    }

    void setBoss(DungeonBoss boss) {
        if (boss != null) components.addAll(boss.getComponents(clazz));
        else components.clear();

        this.boss = boss;
    }

    public DungeonBoss getBoss() {
        return boss;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public List<T> getComponents() {
        return components;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return components.iterator();
    }
}