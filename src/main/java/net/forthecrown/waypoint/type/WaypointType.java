package net.forthecrown.waypoint.type;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.waypoint.Waypoint;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public abstract class WaypointType {
    private final String displayName;

    public void onMove(Waypoint waypoint,
                       Vector3i newPosition,
                       World newWorld
    ) {

    }

    public void onPostMove(Waypoint waypoint) {

    }

    public @NotNull Bounds3i createSize() {
        return Bounds3i.EMPTY;
    }

    public void onDelete(Waypoint waypoint) {

    }

    public Optional<CommandSyntaxException> isValid(Waypoint waypoint) {
        return Optional.empty();
    }

    public Vector3d getVisitPosition(Waypoint waypoint) {
        return waypoint.getBounds()
                .center()
                .withY(waypoint.getBounds().maxY());
    }
}