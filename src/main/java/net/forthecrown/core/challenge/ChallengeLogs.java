package net.forthecrown.core.challenge;

import com.mojang.serialization.Codec;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.log.LogSchema;
import net.forthecrown.log.SchemaField;
import net.forthecrown.utils.io.FtcCodecs;

import java.util.UUID;

public @UtilityClass class ChallengeLogs {
    /* ---------------------------- COMPLETIONS ----------------------------- */

    public final Holder<LogSchema> COMPLETED;

    public final SchemaField<Long> TIME;
    public final SchemaField<UUID> PLAYER;
    public final SchemaField<String> COMPLETED_CHALLENGE;

    static {
        var builder = LogSchema.builder("challenges/completed");

        TIME = builder.add("time", FtcCodecs.TIMESTAMP_CODEC);
        PLAYER = builder.add("player", FtcCodecs.UUID_CODEC);
        COMPLETED_CHALLENGE = builder.add("challenge", FtcCodecs.KEY_CODEC);

        COMPLETED = builder.register();
    }

    /* ------------------------------ ACTIVES ------------------------------- */

    public final Holder<LogSchema> ACTIVE;

    public final SchemaField<String> A_CHALLENGE;
    public final SchemaField<String> A_EXTRA;

    static {
        var builder = LogSchema.builder("challenges/active");

        A_CHALLENGE = builder.add("challenge", Codec.STRING);
        A_EXTRA = builder.add("extra", Codec.STRING);

        ACTIVE = builder.register();
    }

    static void init() {
        // Empty thing to force class load
    }
}