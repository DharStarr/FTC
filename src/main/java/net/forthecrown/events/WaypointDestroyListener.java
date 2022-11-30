package net.forthecrown.events;

import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.waypoint.WaypointManager;
import net.forthecrown.waypoint.WaypointProperties;
import net.forthecrown.waypoint.type.WaypointTypes;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Collection;
import java.util.Collections;

public class WaypointDestroyListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        checkDestroy(Collections.singleton(event.getBlock()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        checkDestroy(event.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        checkDestroy(event.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        checkDestroy(event.getBlocks());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        checkDestroy(event.getBlocks());
    }

    private static void checkDestroy(Collection<Block> blocks) {
        if (blocks.isEmpty()) {
            return;
        }

        var waypoints = WaypointManager.getInstance()
                .getChunkMap()
                .getOverlapping(
                        blocks.iterator().next().getWorld(),
                        Bounds3i.of(blocks)
                );

        waypoints.removeIf(waypoint -> {
            return waypoint.get(WaypointProperties.INVULNERABLE)
                    || waypoint.getType() == WaypointTypes.ADMIN;
        });

        if (waypoints.isEmpty()) {
            return;
        }

        Tasks.runLater(() -> {
            for (var w: waypoints) {
                if (!w.getType().isDestroyed(w)) {
                    continue;
                }

                WaypointManager.getInstance()
                        .removeWaypoint(w);
            }
        }, 1);
    }
}