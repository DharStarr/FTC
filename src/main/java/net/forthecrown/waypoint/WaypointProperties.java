package net.forthecrown.waypoint;

import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.utils.io.types.SerializerParsers;
import net.forthecrown.waypoint.type.PlayerWaypointType;
import net.forthecrown.waypoint.type.WaypointTypes;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.forthecrown.utils.io.types.SerializerParsers.BOOL;
import static net.forthecrown.utils.io.types.SerializerParsers.STRING;

public class WaypointProperties {
    public static final Registry<WaypointProperty> REGISTRY = Registries.newFreezable();

    public static final WaypointProperty<Boolean>
            INVULNERABLE    = new WaypointProperty<>("invulnerable", BOOL, false),
            PUBLIC          = new WaypointProperty<>("public", BOOL, true),
            ALLOWS_MARKER   = new WaypointProperty<>("allowsMarker", BOOL, true),
            SPECIAL_MARKER  = new WaypointProperty<>("specialMarker", BOOL, false);

    public static final WaypointProperty<Boolean>
            HIDE_RESIDENTS = new WaypointProperty<>("hideResidents", BOOL, false)
    {
        @Override
        public void onValueUpdate(Waypoint waypoint,
                                  @Nullable Boolean oldValue,
                                  @Nullable Boolean value
        ) {
            if (waypoint.getType() == WaypointTypes.REGION_POLE) {
                Waypoints.placePole(waypoint);
            }
        }
    };

    public static final WaypointProperty<String>
            NAME = new WaypointProperty<>("name", STRING, null)
    {
        @Override
        public void onValueUpdate(Waypoint waypoint,
                                  @Nullable String oldValue,
                                  @Nullable String value
        ) {
            super.onValueUpdate(waypoint, oldValue, value);

            WaypointManager.getInstance()
                    .onRename(waypoint, oldValue, value);

            if (waypoint.getType() == WaypointTypes.REGION_POLE) {
                Waypoints.placePole(waypoint);
            } else if (waypoint.getType() instanceof PlayerWaypointType) {
                Waypoints.setNameSign(waypoint, value);
            }
        }
    };

    public static final WaypointProperty<UUID>
            OWNER = new WaypointProperty<>("owner", SerializerParsers.UUID, null);

    public static final WaypointProperty<UUID>
            GUILD_OWNER = new WaypointProperty<>("guildOwner", SerializerParsers.UUID, null);

    private static void init() {
        REGISTRY.freeze();
    }
}