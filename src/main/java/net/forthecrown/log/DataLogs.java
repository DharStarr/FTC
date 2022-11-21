package net.forthecrown.log;

import com.mojang.serialization.Dynamic;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public @UtilityClass class DataLogs {
    public static final Registry<LogSchema>
            SCHEMAS = Registries.newRegistry();

    static final DataLog[] EMPTY_LOG_ARR = new DataLog[0];

    public <T> Map<String, Dynamic<T>> asMap(Dynamic<T> dynamic) {
        return dynamic.asMap(
                // This should literally never throw an exception,
                // because what serialization format (that supports
                // DynamicOps) would ever implement a non String2Object
                // map as it's compound map type
                dynamic1 -> dynamic1.asString().getOrThrow(false, s -> {}),

                Function.identity()
        );
    }

    public void log(LogSchema schema, LogEntry entry) {
        DataManager.getInstance()
                .getLogs()
                .log(schema, entry);
    }

    public void log(Holder<LogSchema> schema, LogEntry entry) {
        DataManager.getInstance()
                .getLogs()
                .log(schema, entry);
    }

    public Stream<LogEntry> query(LogQuery query) {
        return DataManager.getInstance()
                .queryLogs(query);
    }
}