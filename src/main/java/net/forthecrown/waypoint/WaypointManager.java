package net.forthecrown.waypoint;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.Getter;
import net.forthecrown.core.*;
import net.forthecrown.core.config.ConfigManager;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.WorldChunkMap;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializableObject;
import net.forthecrown.waypoint.type.WaypointTypes;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.Logger;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static net.forthecrown.user.data.UserTimeTracker.UNSET;

public class WaypointManager extends SerializableObject.NbtDat implements DayChangeListener {
    private static final Logger LOGGER = FTC.getLogger();

    /** The waypoint manager singleton instance */
    @Getter
    private static final WaypointManager instance = new WaypointManager();

    /** Name lookup map */
    private final Map<String, Waypoint> byName = new Object2ObjectOpenHashMap<>();

    /** ID lookup map */
    private final Map<UUID, Waypoint> byId = new Object2ObjectOpenHashMap<>();

    /** Collision and spatial lookup map */
    @Getter
    final WorldChunkMap<Waypoint> chunkMap = new WorldChunkMap<>();

    /* ---------------------------- CONSTRUCTOR ----------------------------- */

    private WaypointManager() {
        super(PathUtil.pluginPath("waypoints.dat"));
    }

    // Reflectively called by Bootstrap
    private static void init() {
        getInstance().reload();

        AutoSave.get().addCallback(getInstance()::save);
        ConfigManager.get().registerConfig(WaypointConfig.class);
        DayChange.get().addListener(getInstance());
    }

    /* ------------------------------ METHODS ------------------------------- */

    @Override
    public void onDayChange(ZonedDateTime time) {
        Set<Waypoint> toRemove = new ObjectArraySet<>();

        for (var w: byId.values()) {
            // Admin waypoints and invulnerable waypoints can
            // never be destroyed, so skip them
            if (w.getType() == WaypointTypes.ADMIN
                    || w.get(WaypointProperties.INVULNERABLE)
            ) {
                continue;
            }

            // If no name nor any residents
            if (Strings.isNullOrEmpty(w.get(WaypointProperties.NAME))
                    && w.getResidents().isEmpty()
            ) {
                if (shouldRemove(w)) {
                    toRemove.add(w);
                }

                continue;
            }

            // Ensure the pole's area is valid
            var result = w.getType().isValid(w);

            if (result.isEmpty()) {
                w.setLastValidTime(System.currentTimeMillis());
                continue;
            }

            if (shouldRemove(w)) {
                toRemove.add(w);
            }
        }

        // No waypoints to remove so stop here
        if (toRemove.isEmpty()) {
            return;
        }

        // Remove all invalid waypoints
        toRemove.forEach(waypoint -> {
            LOGGER.info("Auto-removing waypoint {} or '{}' at {}, world={}",
                    waypoint.getId(),
                    waypoint.get(WaypointProperties.NAME),
                    waypoint.getPosition(),
                    waypoint.getWorld()
            );

            removeWaypoint(waypoint);
        });
    }

    private boolean shouldRemove(Waypoint waypoint) {
        if (waypoint.getLastValidTime() == UNSET) {
            waypoint.setLastValidTime(System.currentTimeMillis());
            return false;
        }

        long deletionTime = waypoint.getLastValidTime()
                + WaypointConfig.waypointDeletionDelay;

        return Time.isPast(deletionTime);
    }

    /** Clears all waypoints */
    public void clear() {
        for (var w: byId.values()) {
            w.manager = null;
        }

        byName.clear();
        byId.clear();
        chunkMap.clear();
    }

    /**
     * Call back for when a region's name is
     * changed to update the name lookup map
     */
    void onRename(Waypoint waypoint, String oldName, String newName) {
        if (!Strings.isNullOrEmpty(oldName)) {
            byName.remove(oldName);
        }

        if (!Strings.isNullOrEmpty(newName)) {
            byName.put(newName.toLowerCase(), waypoint);
        }
    }

    public void addWaypoint(Waypoint waypoint) {
        waypoint.manager = this;
        byId.put(waypoint.getId(), waypoint);

        var name = waypoint.get(WaypointProperties.NAME);
        if (!Strings.isNullOrEmpty(name)) {
            byName.put(name.toLowerCase(), waypoint);

            // Ensure the marker exists, since we have a name
            if (DynmapUtil.isInstalled()) {
                WaypointDynmap.updateMarker(waypoint);
            }
        }

        chunkMap.add(waypoint.getWorld(), waypoint);
    }

    public void removeWaypoint(Waypoint waypoint) {
        waypoint.getType().onDelete(waypoint);

        waypoint.manager = null;
        byId.remove(waypoint.getId());
        chunkMap.remove(waypoint.getWorld(), waypoint);

        var name = waypoint.get(WaypointProperties.NAME);
        if (!Strings.isNullOrEmpty(name)) {
            byName.remove(name.toLowerCase());
        }

        if (DynmapUtil.isInstalled()) {
            WaypointDynmap.removeMarker(waypoint);
        }

        if (!waypoint.getResidents().isEmpty()) {
            waypoint.getResidents()
                    .keySet()
                    .stream()
                    .map(Users::get)
                    .forEach(user -> {
                        user.getHomes().setHomeWaypoint(null);
                        user.unloadIfOffline();
                    });
        }

        UUID guildOwner = waypoint.get(WaypointProperties.GUILD_OWNER);

        if (guildOwner != null) {
            Guild guild = GuildManager.get()
                    .getGuild(guildOwner);

            if (guild != null) {
                guild.getSettings()
                        .setWaypoint(null);
            }
        }

        UUID owner = waypoint.get(WaypointProperties.OWNER);
        if (owner != null) {
            User user = Users.get(owner);
            user.getHomes().setHomeWaypoint(null);
            user.unloadIfOffline();
        }
    }

    public Waypoint get(UUID uuid) {
        return byId.get(uuid);
    }

    public Waypoint get(String name) {
        return byName.get(name.toLowerCase());
    }

    public Stream<String> getNames() {
        return byName.values()
                .stream()
                .map(waypoint -> waypoint.get(WaypointProperties.NAME));
    }

    /* --------------------------- SERIALIZATION ---------------------------- */

    @Override
    protected void load(CompoundTag tag) {
        clear();

        for (var e: tag.tags.entrySet()) {
            Waypoint waypoint = new Waypoint(UUID.fromString(e.getKey()));
            waypoint.load((CompoundTag) e.getValue());

            addWaypoint(waypoint);
        }
    }

    @Override
    protected void save(CompoundTag tag) {
        for (var w: byId.values()) {
            var cTag = new CompoundTag();
            w.save(cTag);

            tag.put(w.getId().toString(), cTag);
        }
    }
}