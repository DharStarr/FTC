package net.forthecrown.dungeons.level.decoration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.math.Vectors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector3i;

@Getter
@RequiredArgsConstructor
public class DungeonPopulator {
    private final SpawnerImpl spawner;
    private final Vector3i position;

    public void onTick(World world) {
        spawner.serverTick(
                VanillaAccess.getLevel(world),
                Vectors.toMinecraft(position)
        );
    }

    public CompoundTag save() {

    }

    public static DungeonPopulator load(Tag t) {
        if (!(t instanceof CompoundTag tag)) {
            return null;
        }


    }

    public static class SpawnerImpl extends BaseSpawner {
        @Override
        public void broadcastEvent(Level world, BlockPos pos, int status) {
            world.blockEvent(pos, Blocks.SPAWNER, status, 0);
        }
    }
}