package net.forthecrown.waypoint;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.DynmapUtil;
import net.forthecrown.core.FtcDynmap;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.text.Text;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.Objects;
import java.util.UUID;

/**
 * Methods relating to Waypoints and their
 * Dynmap markers.
 * <p>
 * This class should only ever be loaded if
 * dynmap is installed on the server, this can
 * be tested with {@link DynmapUtil#isInstalled()}
 */
@UtilityClass class WaypointDynmap {
    /** Waypoint Marker set ID */
    public static final String SET_ID = "waypoint_marker_set";

    /** Waypoint marker set display name */
    public static final String SET_NAME = "Waypoints";

    /**
     * Waypoint HTML description's field color
     * @see #createHtmlDescription(Waypoint)
     */
    public static final String FIELD_COLOR = "#e67e22";

    /**
     * Updates the marker of the given waypoint.
     * <p>
     * If the waypoint doesn't have a name, the marker
     * is deleted, if it exists.
     * <p>
     * If the marker should exist, but doesn't, it's
     * created, if it does exist, it's data is updated
     * to be in sync with the actual waypoint
     */
    static void updateMarker(Waypoint waypoint) {
        var set = getSet();
        var name = waypoint.get(WaypointProperties.NAME);
        var marker = set.findMarker(waypoint.getMarkerId());

        if (Strings.isNullOrEmpty(name)
                || !waypoint.get(WaypointProperties.ALLOWS_MARKER)
        ) {
            if (marker != null) {
                marker.deleteMarker();
            }

            return;
        }

        MarkerIcon icon = waypoint.get(WaypointProperties.SPECIAL_MARKER)
                ? FtcDynmap.getSpecialIcon()
                : FtcDynmap.getNormalIcon();

        if (marker == null) {
            marker = set.createMarker(
                    waypoint.getMarkerId(),
                    waypoint.get(WaypointProperties.NAME),

                    // Location
                    waypoint.getWorld().getName(),
                    waypoint.getPosition().x(),
                    waypoint.getPosition().y(),
                    waypoint.getPosition().z(),

                    // Icon
                    icon,

                    // Persistent
                    true
            );
        } else {
            marker.setLabel(name);
            marker.setMarkerIcon(icon);

            marker.setLocation(
                    waypoint.getWorld().getName(),
                    waypoint.getPosition().x(),
                    waypoint.getPosition().y(),
                    waypoint.getPosition().z()
            );
        }

        marker.setDescription(
                createHtmlDescription(waypoint)
        );
    }

    private static String createHtmlDescription(Waypoint waypoint) {
        StringBuilder buffer = new StringBuilder();

        buffer.append(waypoint.get(WaypointProperties.NAME))
                .append(htmlSpan(FIELD_COLOR, ":"));

        var owner = waypoint.get(WaypointProperties.OWNER);
        if (owner != null) {
            User ownerUser = Users.get(owner);
            ownerUser.unloadIfOffline();

            buffer.append(htmlField("Owner", Text.plain(ownerUser.displayName())));
        }

        UUID guildOwner = waypoint.get(WaypointProperties.GUILD_OWNER);
        if (guildOwner != null) {
            Guild guild = GuildManager.get().getGuild(guildOwner);

            if (guild != null && !Strings.isNullOrEmpty(guild.getName())) {
                buffer.append(
                        htmlField("Guild Owner", guild.getName())
                );
            }
        }

        if (!waypoint.getResidents().isEmpty()) {
            buffer.append(htmlField("Residents", waypoint.getResidents().size()));
        }

        buffer.append(htmlField("Type", waypoint.getType().getDisplayName()));
        return buffer.toString();
    }

    private static String htmlField(String field, Object value) {
        if (value == null) {
            return "";
        }

        return htmlLine(field + ": " + htmlSpan(FIELD_COLOR, Objects.toString(value)));
    }

    private static String htmlSpan(String color, String content) {
        if (Strings.isNullOrEmpty(color)) {
            return content;
        }

        return "<span style=\"color=" + color + ";>" + content + "<\\span>";
    }

    private static String htmlLine(String s) {
        return "<p>" + s + "<\\p>";
    }

    static void removeMarker(Waypoint waypoint) {
        var marker = getMarker(waypoint);

        if (marker == null) {
            return;
        }

        marker.deleteMarker();
    }

    static Marker getMarker(Waypoint waypoint) {
        return getSet().findMarker(waypoint.getMarkerId());
    }

    static MarkerSet getSet() {
        var api = FtcDynmap.getMarkerAPI();

        return Objects.requireNonNullElseGet(
                api.getMarkerSet(SET_ID),
                () -> api.createMarkerSet(SET_ID, SET_NAME, null, true)
        );
    }
}