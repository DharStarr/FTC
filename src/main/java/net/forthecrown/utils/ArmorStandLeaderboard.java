package net.forthecrown.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.registry.Keys;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Accessors(chain = true)
@RequiredArgsConstructor
public class ArmorStandLeaderboard<T> {
    public static final NamespacedKey
            STAND_KEY = Keys.forthecrown("leaderboard");

    public static final double LINE_DISTANCE = 0.25;

    private final Object2IntMap<T> values = new Object2IntOpenHashMap<>();

    private final List<Reference<ArmorStand>>
            armorStands = new ObjectArrayList<>();

    private final Location location;

    private Reference<World> world;
    private BoundingBox bounds;

    @Setter
    private int maxSize = 5;

    @Setter
    private Comparator<Map.Entry<T, Integer>>
            comparator = Map.Entry.<T, Integer>comparingByValue().reversed();

    @Setter
    private LineFormatter<T> lineFormatter;

    @Setter
    private double headerSeparation = 0.0D;
    @Setter
    private double footerSeparation = 0.0D;

    @Setter
    private double lineDistance = LINE_DISTANCE;

    private final List<Component> header = new ObjectArrayList<>();
    private final List<Component> footer = new ObjectArrayList<>();

    public void spawn() {
        if (isSpawned()) {
            kill();
        }

        Objects.requireNonNull(lineFormatter, "No line formatter");
        Objects.requireNonNull(location, "No set location");
        Objects.requireNonNull(location.getWorld(), "No world in location");

        int size = Math.min(maxSize, values.size());

        double totalSize = size + header.size() + footer.size();
        double hSeparation = header.isEmpty() ? 0 : headerSeparation;
        double fSeparation = footer.isEmpty() ? 0 : footerSeparation;
        double yOffset = (totalSize * lineDistance) + hSeparation + fSeparation;

        Location l = location.clone();
        l.add(0, yOffset, 0);

        spawnList(l, header);
        l.subtract(0, hSeparation, 0);

        if (!values.isEmpty()) {
            List<Object2IntMap.Entry<T>>
                    entries = new ObjectArrayList<>(values.object2IntEntrySet());

            entries.sort(comparator);
            var subList = entries.subList(0, size);
            List<Component> components = new ObjectArrayList<>();

            var it = subList.listIterator();

            while (it.hasNext()) {
                var next = it.next();
                int index = it.nextIndex();

                Component c = lineFormatter.formatLine(
                        index, next.getKey(), next.getIntValue()
                );

                components.add(c);
            }

            spawnList(l, components);
        }

        l.subtract(0, fSeparation, 0);
        spawnList(l, footer);

        if (bounds != null) {
            bounds.expand(0.5D);
        }
    }

    private void spawnList(Location l, List<Component> components) {
        var it = components.iterator();

        if (!it.hasNext()) {
            return;
        }

        while (it.hasNext()) {
            var next = it.next();

            if (!Objects.equals(next, Component.empty())) {
                var entity = l.getWorld().spawn(l, ArmorStand.class, stand -> {
                    stand.setMarker(true);
                    stand.setBasePlate(false);
                    stand.setInvisible(true);
                    stand.setInvulnerable(true);
                    stand.setCustomNameVisible(true);
                    stand.setCanTick(false);
                    stand.getPersistentDataContainer()
                            .set(STAND_KEY, PersistentDataType.INTEGER, 1);

                    stand.customName(next);
                });

                armorStands.add(new WeakReference<>(entity));
                world = new WeakReference<>(entity.getWorld());

                if (bounds == null) {
                    bounds = entity.getBoundingBox().clone();
                } else {
                    bounds.union(entity.getBoundingBox());
                }
            }

            l.subtract(0, LINE_DISTANCE, 0);
        }
    }

    public void kill() {
        if (!isSpawned()) {
            return;
        }

        armorStands.forEach(reference -> reference.get().remove());
        armorStands.clear();

        world.get().getNearbyEntities(bounds, entity -> entity.getPersistentDataContainer().has(STAND_KEY))
                .forEach(Entity::remove);

        world = null;
        bounds = null;
    }

    public boolean isSpawned() {
        armorStands.removeIf(reference -> {
            return reference == null || reference.get() == null;
        });

        return world != null
                && world.get() != null
                && bounds != null;
    }

    public ArmorStandLeaderboard<T> addHeader(Component c) {
        header.add(c);
        return this;
    }

    public ArmorStandLeaderboard<T> addFooter(Component c) {
        footer.add(c);
        return this;
    }

    @FunctionalInterface
    public interface LineFormatter<T> {
        Component formatLine(int index, T entry, int score);
    }
}