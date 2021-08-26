package net.forthecrown.events;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.utils.Worlds;
import net.forthecrown.utils.math.MathUtil;
import net.minecraft.core.BlockPos;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class RegionsListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getPlayer().hasPermission(Permissions.REGIONS_ADMIN)) return;
        if(!event.getPlayer().getWorld().equals(Worlds.OVERWORLD)) return;

        Block block = event.getBlock();
        PopulationRegion region = Crown.getRegionManager().get(RegionPos.fromAbsolute(block.getX(), block.getZ()));

        if(region.getPoleBoundingBox().isInside(new BlockPos(block.getX(), block.getY(), block.getZ()))) {
            event.setCancelled(true);
        }
    }

    private final RegionManager manager = Crown.getRegionManager();

    @EventHandler(ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        //Get block cords from chunk cords
        int x = chunk.getX() << 4;
        int z = chunk.getZ() << 4;

        //Get pos from those cords
        RegionPos pos = RegionPos.fromAbsolute(x, z);
        PopulationRegion region = manager.get(pos);

        //Get chunk's 2D bounding box
        BoundingBox2D chunkRegion = new BoundingBox2D(x, z, x + 16, z + 16);

        //If it doesn't contain the region pole, stop
        if(!chunkRegion.contains(region.getPolePosition())) return;

        //If it does, however, generate a region pole
        manager.getGenerator().generate(region);
    }

    private static class BoundingBox2D {
        private final int minX;
        private final int minZ;
        private final int maxX;
        private final int maxZ;

        private BoundingBox2D(int minX, int minZ, int maxX, int maxZ) {
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxZ = maxZ;
        }

        public boolean contains(BlockVector2 vec2) {
            int x = vec2.getX();
            int z = vec2.getZ();

            return MathUtil.isInRange(x, minX, maxX) && MathUtil.isInRange(z, minZ, maxZ);
        }
    }
}