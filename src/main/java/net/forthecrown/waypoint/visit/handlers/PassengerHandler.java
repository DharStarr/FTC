package net.forthecrown.waypoint.visit.handlers;

import net.forthecrown.waypoint.visit.RidingNode;
import net.forthecrown.waypoint.visit.VisitHandler;
import net.forthecrown.waypoint.visit.WaypointVisit;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;

public class PassengerHandler implements VisitHandler {
    RidingNode root;

    @Override
    public void onStart(WaypointVisit visit) {
        Player player = visit.getUser().getPlayer();
        root = RidingNode.create(player);

        if (ArrayUtils.isEmpty(root.getPassengers())) {
            visit.setHulkSmashSafe(true);
            root = null;
        } else {
            visit.setHulkSmashSafe(false);

            root.forEach(entity -> {
                visit.modifyHandler(
                        OwnedEntityHandler.class,
                        handler -> handler.ignored.add(entity.getUniqueId())
                );
            });
        }
    }

    @Override
    public void onTeleport(WaypointVisit visit) {
        if (root != null) {
            root.remount(null);
        }
    }
}