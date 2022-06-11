package net.forthecrown.utils.math;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class WorldBounds3i extends AbstractBounds3i<WorldBounds3i> implements Iterable<Block> {
    private World world;

    public WorldBounds3i(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ, true);
        this.world = world;
    }

    protected WorldBounds3i(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean immutable) {
        super(minX, minY, minZ, maxX, maxY, maxZ, immutable);
        this.world = world;
    }

    public static WorldBounds3i of(World world, Region region) {
        return of(world, region.getMinimumPoint(), region.getMaximumPoint());
    }

    public static WorldBounds3i of(World world, BlockVector3 vec1, BlockVector3 vec2) {
        return new WorldBounds3i(world,
                vec1.getX(),
                vec1.getY(),
                vec1.getZ(),
                vec2.getX(),
                vec2.getY(),
                vec2.getZ()
        );
    }

    public static WorldBounds3i of(World world, ImmutableVector3i vec1, ImmutableVector3i vec2) {
        return new WorldBounds3i(world,
                vec1.getX(),
                vec1.getY(),
                vec1.getZ(),
                vec2.getX(),
                vec2.getY(),
                vec2.getZ()
        );
    }

    public static WorldBounds3i of(CompoundTag tag) {
        int[] cords = tag.getIntArray("cords");
        World world = Bukkit.getWorld(tag.getString("world"));

        return new WorldBounds3i(world, cords[0], cords[1], cords[2], cords[3], cords[3], cords[5]);
    }

    public World getWorld() {
        return world;
    }

    public WorldBounds3i setWorld(World world) {
        if(immutable) {
            return new WorldBounds3i(world, minX, minY, minZ, maxX, maxY, maxZ, true);
        }

        this.world = world;
        return this;
    }

    @Override
    protected WorldBounds3i getThis() {
        return this;
    }

    @Override
    protected WorldBounds3i cloneAt(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean immutable) {
        return new WorldBounds3i(world, minX, minY, minZ, maxX, maxY, maxZ, immutable);
    }

    public WorldVec3i worldMin() {
        return min().toWorldVector(world);
    }

    public WorldVec3i worldMax() {
        return max().toWorldVector(world);
    }

    public WorldVec3i worldCenter() {
        return center().toWorldVector(world);
    }

    @Override
    public boolean contains(Location vec) {
        if(!world.equals(vec.getWorld())) return false;
        return super.contains(vec);
    }

    @Override
    public boolean contains(ImmutableVector3i vec) {
        if(vec instanceof WorldVec3i wVec) {
            if(!world.equals(wVec.getWorld())) return false;
        }

        return super.contains(vec);
    }

    @Override
    public boolean contains(ImmutableBounds3i o) {
        if(o instanceof WorldBounds3i w) {
            if(!world.equals(w.getWorld())) return false;
        }

        return super.contains(o);
    }

    @Override
    public boolean overlaps(ImmutableBounds3i o) {
        if(o instanceof WorldBounds3i w) {
            if(!world.equals(w.getWorld())) return false;
        }

        return super.overlaps(o);
    }

    @NotNull
    @Override
    public BlockIterator iterator() {
        return new BlockIterator(getWorld(), min(), max(), volume());
    }

    public Iterator<WorldVec3i> vectorIterator() {
        return new VectorIterator<>(worldMin(), worldMax(), volume());
    }

    public Collection<Entity> getEntities() {
        return getWorld().getNearbyEntities(toBukkit());
    }

    public Collection<Entity> getEntities(Predicate<Entity> predicate) {
        return getWorld().getNearbyEntities(toBukkit(), predicate);
    }

    public <T extends Entity> Collection<T> getEntitiesByType(Class<T> clazz) {
        return getEntitiesByType(clazz, null);
    }

    public <T extends Entity> Collection<T> getEntitiesByType(Class<T> clazz, Predicate<T> predicate) {
        List<T> nearby = new ObjectArrayList<>();

        for (Entity entity : getEntities()) {
            if (!clazz.isAssignableFrom(entity.getClass())) continue;
            T ent = (T) entity;

            if(predicate != null && !predicate.test(ent)) continue;

            nearby.add(ent);
        }

        return nearby;
    }

    public Collection<LivingEntity> getLivingEntities() {
        return getEntitiesByType(LivingEntity.class);
    }

    public Collection<Player> getPlayers() {
        return getEntitiesByType(Player.class);
    }

    @Override
    public Tag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("world", world.getName());
        tag.put("cords", super.save());

        return tag;
    }

    @Override
    public String toString() {
        return "(" + getWorld().getName() + ", " + super.toString() + ")";
    }
}