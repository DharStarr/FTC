package net.forthecrown.guilds;

import lombok.experimental.UtilityClass;
import net.forthecrown.core.FtcDynmap;
import net.forthecrown.utils.math.Vectors;
import net.minecraft.world.level.ChunkPos;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

public @UtilityClass class GuildDynmap {
    private static final String
            MARKER_SET_ID = "chunk_markers",
            MARKER_LABEL = "Guild Areas";

    private MarkerSet getSet() {
        var markers = FtcDynmap.getMarkerAPI();
        var set = markers.getMarkerSet(MARKER_SET_ID);

        if (set != null) {
            return set;
        }

        return markers.createMarkerSet(MARKER_SET_ID, MARKER_LABEL, null, true);
    }

    public void renderChunk(ChunkPos chunkPos, Guild guild) {
        String markerId = chunkId(chunkPos);
        var set = getSet();

        if (set.findAreaMarker(markerId) != null) {
            return;
        }

        double[] xCorners = getCorners(chunkPos.x);
        double[] zCorners = getCorners(chunkPos.z);

        AreaMarker marker = set.createAreaMarker(
                markerId,
                guild.getName(),
                true,
                Guilds.getWorld().getName(),
                xCorners, zCorners,
                true
        );

        int color;
        try {
            color = guild.getSettings()
                    .getPrimaryColor()
                    .getTextColor()
                    .value();
        } catch (Exception ignored) {
            color = marker.getFillColor();
        }


        marker.setFillStyle(0.5, color);
        marker.setLineStyle(marker.getLineWeight(), 0.8, color);
    }

    public void unrenderChunk(ChunkPos chunkPos) {
        String markerId = chunkId(chunkPos);

        var markerSet = getSet();
        AreaMarker marker = markerSet.findAreaMarker(markerId);

        if (marker != null) {
            marker.deleteMarker();
        }
    }

    private String chunkId(ChunkPos pos) {
        return MARKER_SET_ID + "::" + pos.x + "_" + pos.z;
    }

    private double[] getCorners(int chunkCoord) {
        int xBlock = Vectors.toBlock(chunkCoord);

        return new double[] {
                xBlock,
                xBlock + Vectors.CHUNK_SIZE
        };
    }
}