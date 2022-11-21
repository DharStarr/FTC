package net.forthecrown.waypoint.visit.handlers;

import net.forthecrown.waypoint.visit.RidingNode;
import net.forthecrown.waypoint.visit.WaypointVisit;
import net.forthecrown.waypoint.visit.VisitHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

public class RidingVehicleHandler implements VisitHandler {
    RidingNode root;

    @Override
    public void onStart(WaypointVisit visit) {
        Player player = visit.getUser().getPlayer();
        boolean riding = ridingVehicle(player);
        visit.setHulkSmashSafe(!riding);

        if (riding) {
            Entity vehicle = player.getVehicle();

            root = RidingNode.create(vehicle);
            root.forEach(entity -> {
                visit.modifyHandler(
                        OwnedEntityHandler.class,
                        handler -> handler.ignored.add(entity.getUniqueId())
                );
            });

        } else {
            root = null;
        }
    }

    @Override
    public void onTeleport(WaypointVisit visit) {
        if (root == null
                || ArrayUtils.isEmpty(root.getPassengers())
                || visit.hulkSmash()
        ) {
            return;
        }

        root.remount(null);
    }

    boolean ridingVehicle(Player player) {
        if (!player.isInsideVehicle()) {
            return false;
        }

        return player.getVehicle() instanceof Vehicle;
    }
}