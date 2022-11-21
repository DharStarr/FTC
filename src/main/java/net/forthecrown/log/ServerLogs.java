package net.forthecrown.log;

import com.mojang.serialization.Codec;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.utils.io.FtcCodecs;
import org.bukkit.Location;

import java.util.UUID;

public class ServerLogs {
    public static final Holder<LogSchema> JOIN_SCHEMA;

    public static final SchemaField<UUID> J_PLAYER;
    public static final SchemaField<Long> J_TIME;
    public static final SchemaField<Boolean> J_FIRST_JOIN;
    public static final SchemaField<Location> J_LOCATION;

    static {
        var builder = LogSchema.builder("player_joins");

        J_PLAYER = builder.add("player", FtcCodecs.UUID_CODEC);
        J_TIME = builder.add("time", FtcCodecs.TIMESTAMP_CODEC);
        J_FIRST_JOIN = builder.add("first_join", Codec.BOOL);
        J_LOCATION = builder.add("location", FtcCodecs.LOCATION_CODEC);

        JOIN_SCHEMA = builder.register();
    }
}