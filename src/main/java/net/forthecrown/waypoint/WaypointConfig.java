package net.forthecrown.waypoint;

import lombok.experimental.UtilityClass;
import net.forthecrown.core.config.ConfigData;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector3i;

import java.util.concurrent.TimeUnit;

@ConfigData(filePath = "waypoints.json")
public @UtilityClass class WaypointConfig {
    public Vector3i
            playerWaypointSize      = Vector3i.from(5);

    public String
            spawnWaypoint           = "Hazelguard";

    public String[]
            disabledPlayerWorlds    = { "world_void", "world_resource", "world_the_end" };

    public long
            waypointDeletionDelay   = TimeUnit.DAYS.toMillis(7);

    public boolean isDisabledWorld(World w) {
        return ArrayUtils.contains(disabledPlayerWorlds, w.getName());
    }
}