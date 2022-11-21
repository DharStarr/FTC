package net.forthecrown.waypoint.visit;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;

public interface VisitPredicate {
    VisitPredicate RIDING_VEHICLE = visit -> {
        Entity entity = visit.getUser().getPlayer().getVehicle();
        if (entity == null || entity instanceof Vehicle) {
            return;
        }

        throw Exceptions.ONLY_IN_VEHICLE;
    };

    VisitPredicate IS_NEAR = visit -> {
        var player = visit.getUser();

        if (player.hasPermission(Permissions.WAYPOINTS_ADMIN)) {
            return;
        }

        var nearest = visit.getNearestWaypoint();

        if (!visit.isNearWaypoint()) {
            if (nearest == null) {
                throw Exceptions.FAR_FROM_WAYPOINT;
            } else {
                var pos = nearest.getPosition();
                throw Exceptions.farFromWaypoint(pos.x(), pos.z());
            }
        } else {
            var validTest = nearest.getType()
                    .isValid(nearest);

            if (validTest.isEmpty()) {
                return;
            }

            player.sendMessage(
                    Component.text("Cannot use this pole:", NamedTextColor.RED)
            );
            throw validTest.get();
        }
    };

    VisitPredicate DESTINATION_EXISTS = visit -> {
        if (!visit.getDestination().isWorldLoaded()) {
            throw Exceptions.UNLOADED_WORLD;
        }

        var exc = visit.getDestination()
                .getType()
                .isValid(visit.getDestination());

        if (exc.isPresent()) {
            throw exc.get();
        }
    };

    /**
     * Tests if the visit is allowed to continue
     * <p></p>
     * Predicates are the first thing called when a
     * region visit is ran
     * @param visit The visit to check
     * @throws CommandSyntaxException If the check failed
     */
    void test(WaypointVisit visit) throws CommandSyntaxException;
}