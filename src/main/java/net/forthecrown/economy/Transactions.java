package net.forthecrown.economy;

import com.google.common.base.Strings;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.log.DataLogs;
import net.forthecrown.log.LogEntry;
import net.forthecrown.log.LogSchema;
import net.forthecrown.log.SchemaField;
import net.forthecrown.utils.io.FtcCodecs;

import java.util.Objects;

public @UtilityClass class Transactions {
    public final Holder<LogSchema> TRANSACTION_SCHEMA;

    public final SchemaField<String>            T_TARGET;
    public final SchemaField<String>            T_SENDER;
    public final SchemaField<String>            T_EXTRA;
    public final SchemaField<Integer>           T_AMOUNT;
    public final SchemaField<Long>              T_TIME;
    public final SchemaField<TransactionType>   T_TYPE;

    static {
        var builder = LogSchema.builder("economy/transactions");

        T_TARGET = builder.add("target", Codec.STRING);
        T_SENDER = builder.add("sender", Codec.STRING);
        T_EXTRA  = builder.add("extra",  Codec.STRING);

        T_AMOUNT = builder.add("amount", Codec.INT);
        T_TIME = builder.add("timestamp", FtcCodecs.TIMESTAMP_CODEC);
        T_TYPE = builder.add(
                "type",
                FtcCodecs.enumCodec(TransactionType.class)
        );

        TRANSACTION_SCHEMA = builder.register();
    }

    // Called reflectively by BootStrap
    private void init() {
        // Force class load
    }

    public TransactionBuilder builder() {
        return new TransactionBuilder(System.currentTimeMillis());
    }

    @Setter @Getter
    @Accessors(fluent = true, chain = true)
    @RequiredArgsConstructor
    public class TransactionBuilder {
        private String target;
        private String sender;
        private String extra;
        private int amount;
        private final long time;
        private TransactionType type;

        public TransactionBuilder sender(Object o) {
            this.sender = (o == null) ? null : String.valueOf(o);
            return this;
        }

        public TransactionBuilder target(Object o) {
            this.target = (o == null) ? null : String.valueOf(o);
            return this;
        }

        public TransactionBuilder extra(String s, Object... args) {
            this.extra = String.format(s, args);
            return this;
        }

        public void log() {
            Objects.requireNonNull(type, "Type not given");

            LogEntry entry = LogEntry.of(TRANSACTION_SCHEMA)
                    .set(T_TIME, time)
                    .set(T_TYPE, type);

            if (!Strings.isNullOrEmpty(target)) {
                entry.set(T_TARGET, target);
            }

            if (!Strings.isNullOrEmpty(sender)) {
                entry.set(T_SENDER, sender);
            }

            if (!Strings.isNullOrEmpty(extra)) {
                entry.set(T_EXTRA, extra);
            }

            if (amount != 0) {
                entry.set(T_AMOUNT, amount);
            }

            DataLogs.log(TRANSACTION_SCHEMA, entry);
        }
    }
}