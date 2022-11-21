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
        visit.setHulkSmashSafe(player.getPassengers().isEmpty());

        root = RidingNode.create(player);

        if (ArrayUtils.isEmpty(root.getPassengers())) {
            root = null;
        }
    }

    @Override
    public void onTeleport(WaypointVisit visit) {
        if (root != null && !visit.hulkSmash()) {
            root.remount(null);
        }
    }
}