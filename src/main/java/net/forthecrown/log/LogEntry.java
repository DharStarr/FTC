package net.forthecrown.log;

import lombok.RequiredArgsConstructor;
import net.forthecrown.core.registry.Holder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class LogEntry {
    final Object[] values;

    public static LogEntry of(Holder<LogSchema> holder) {
        return of(holder.getValue());
    }

    public static LogEntry of(LogSchema schema) {
        return new LogEntry(new Object[schema.getFields().length]);
    }

    public <T> LogEntry set(@NotNull SchemaField<T> field, @Nullable T value) {
        values[field.id()] = value;
        return this;
    }

    public <T> @Nullable T get(@NotNull SchemaField<T> field) {
        return (T) values[field.id()];
    }
}