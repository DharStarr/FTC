package net.forthecrown.log;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public class DataLog {
    private static final Logger LOGGER = FTC.getLogger();

    private final LogSchema schema;
    private final List<LogEntry> entries = new ObjectArrayList<>();

    public void performQuery(LogQuery query,
                             Stream.Builder<LogEntry> builder,
                             AtomicInteger found
    ) {
        entries.stream()
                .filter(query)
                .forEach(entry -> {
                    found.incrementAndGet();
                    builder.add(entry);
                });
    }

    public void add(LogEntry entry) {
        entries.add(entry);
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public <S> S serialize(DynamicOps<S> ops) {
        return ops.createList(
                entries.stream()
                        .map(entry -> {
                            return schema.serialize(ops, entry)
                                    .resultOrPartial(LOGGER::warn)
                                    .orElse(null);
                        })

                        .filter(Objects::nonNull)
        );
    }

    public <S> void deserialize(Dynamic<S> dynamic) {
        entries.clear();

        entries.addAll(
                dynamic.asList(dynamic1 -> {
                    return schema.deserialize(dynamic1)
                            .resultOrPartial(LOGGER::error)
                            .orElseThrow();
                })
        );
    }
}