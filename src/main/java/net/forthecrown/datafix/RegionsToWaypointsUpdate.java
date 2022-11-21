package net.forthecrown.datafix;

import com.google.common.base.Strings;
import net.forthecrown.core.DynmapUtil;
import net.forthecrown.core.FtcDynmap;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Worlds;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.WaypointManager;
import net.forthecrown.waypoint.WaypointProperties;
import net.forthecrown.waypoint.type.WaypointTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.bukkit.permissions.Permission;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3i;

import java.util.UUID;

public class RegionsToWaypointsUpdate extends DataUpdater {
    public static final int
            FLAG_SPECIAL = 0x1,
            FLAG_NO_MARKER = 0x2,
            FLAG_HIDE_RESIDENTS = 0x4,
            FLAG_PRIVATE = 0x8,

            INDEX_MIN_Y = 1,

            REGION_SIZE = 400,
            REGION_HALF_SIZE = REGION_SIZE / 2;

    public static final String
            TAG_POLE_BOUNDS = "poleBounds",
            TAG_NAME = "name",
            TAG_RESIDENCY = "residency",
            TAG_PROPERTIES = "properties",
            TAG_POLE_POS = "polePosition",

            TAG_RESIDENT = "resident",
            TAG_MOVE_IN = "directMoveIn";

    @Override
    protected boolean update() throws Throwable {
        var path = PathUtil.pluginPath("regions.dat");
        var manager = WaypointManager.getInstance();

        CompoundTag tag = SerializationHelper.TAG_READER.apply(path);

        PathUtil.safeDelete(path)
                .resultOrPartial(LOGGER::error);

        for (var v: tag.tags.entrySet()) {
            try {
                CompoundTag rTag = (CompoundTag) v.getValue();

                String[] stringPos = v.getKey().split(" ");
                int xPos = (Integer.parseInt(stringPos[0]) * REGION_SIZE) + REGION_HALF_SIZE;
                int zPos = (Integer.parseInt(stringPos[1]) * REGION_SIZE) + REGION_HALF_SIZE;

                int flags = rTag.getInt(TAG_PROPERTIES);
                boolean special = hasMask(flags, FLAG_SPECIAL);
                boolean noMarker = hasMask(flags, FLAG_NO_MARKER);
                boolean _private = hasMask(flags, FLAG_PRIVATE);
                boolean hideResidents = hasMask(flags, FLAG_HIDE_RESIDENTS);

                String name = Strings.emptyToNull(rTag.getString(TAG_NAME));

                if (rTag.contains(TAG_POLE_POS)) {
                    Vector2i polePos = Vectors.read2i(rTag.get(TAG_POLE_POS));
                    xPos = polePos.x();
                    zPos = polePos.y();
                }

                int yPos = rTag.getIntArray(TAG_POLE_BOUNDS)[INDEX_MIN_Y];

                Vector3i pos = Vector3i.from(xPos, yPos, zPos);

                Waypoint waypoint = new Waypoint();
                waypoint.set(WaypointProperties.NAME, name);
                waypoint.set(WaypointProperties.PUBLIC, !_private);
                waypoint.set(WaypointProperties.SPECIAL_MARKER, special);
                waypoint.set(WaypointProperties.ALLOWS_MARKER, !noMarker);
                waypoint.set(WaypointProperties.HIDE_RESIDENTS, hideResidents);

                // Hardcoded exception for spawn region
                // and guardian region to be invulnerable
                if (name != null
                        && (name.equals("Hazelguard") || name.equals("Guardian"))
                ) {
                    waypoint.set(WaypointProperties.INVULNERABLE, true);
                }

                waypoint.setType(WaypointTypes.REGION_POLE);
                waypoint.setPosition(pos, Worlds.overworld());

                updateResidency(
                        rTag.getList(TAG_RESIDENCY, Tag.TAG_COMPOUND),
                        waypoint
                );

                manager.addWaypoint(waypoint);

                LOGGER.info("Updated region '{}' at Region(x={}, z={}), polePos={}",
                        name, stringPos[0], stringPos[1], pos
                );
            } catch (Throwable t) {
                LOGGER.error("Couldn't update region at {}", v.getKey(), t);
            }
        }

        if (DynmapUtil.isInstalled()) {
            var set = FtcDynmap.getMarkerAPI()
                    .getMarkerSet("region_poles");

            if (set != null) {
                set.deleteMarkerSet();
                LOGGER.info("Deleted region pole dynmap marker set");
            }
        } else {
            LOGGER.warn("Dynmap plugin not found, cannot delete region pole marker set!");
        }

        updateGroupPerm("default", "ftc.regions", Permissions.WAYPOINTS);
        updateGroupPerm("helper", "ftc.regions", Permissions.WAYPOINTS);
        updateGroupPerm("helper", "ftc.regions.admin", Permissions.WAYPOINTS_ADMIN);

        return true;
    }

    private void updateResidency(ListTag tag, Waypoint waypoint) {
        for (var t: tag) {
            CompoundTag rTag = (CompoundTag) t;

            if (!rTag.contains(TAG_MOVE_IN)) {
                continue;
            }

            UUID resident = rTag.getUUID(TAG_RESIDENT);
            long moveIn = rTag.getLong(TAG_MOVE_IN);

            waypoint.setResident(resident, moveIn);

            try {
                User user = Users.get(resident);

                user.getHomes().setHomeWaypoint(waypoint);
                user.unloadIfOffline();
            } catch (RuntimeException exc) {
                LOGGER.error("Couldn't update personal data of user {}", resident, exc);
            }
        }
    }

    private boolean hasMask(int flags, int flag) {
        return (flags & flag) == flag;
    }

    private void updateGroupPerm(String group, String old, Permission permission) {
        Util.consoleCommand("lp group %s permission unset %s", group, old);
        Util.consoleCommand("lp group %s permission set %s true", group, permission.getName());

        LOGGER.info("Updated group perms! Removed perm '{}' from {} and added {}",
                old, group, permission.getName()
        );
    }
}