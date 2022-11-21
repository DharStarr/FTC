package net.forthecrown.log;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.registry.Holder;
import org.apache.commons.lang3.Range;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Objects;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public class LogQuery implements Predicate<LogEntry> {
    private final Predicate[] predicates;
    private final Holder<LogSchema> schema;
    private final Range<ChronoLocalDate> searchRange;
    private final int maxResults;

    @Override
    public boolean test(LogEntry entry) {
        for (var f: schema.getValue().getFields()) {
            var predicates = this.predicates[f.id()];

            if (predicates == null) {
                continue;
            }

            if (!predicates.test(entry.get(f))) {
                return false;
            }
        }

        return true;
    }

    public static Builder<?> builder(Holder<LogSchema> schema) {
        return new Builder<>(schema);
    }

    @Setter
    @Accessors(fluent = true, chain = true)
    public static class Builder<F> {
        private final Predicate[] predicates;
        private final Holder<LogSchema> schema;

        private int maxResults = Integer.MAX_VALUE;
        private Range<ChronoLocalDate> queryRange;

        public Builder(Holder<LogSchema> schema) {
            this.schema = schema;
            this.predicates = new Predicate[schema.getValue().getFields().length];
            this.queryRange = Range.is(LocalDate.now());
        }

        private SchemaField lastField;

        public <T> Builder<T> field(SchemaField<T> field) {
            this.lastField = field;
            return (Builder<T>) this;
        }

        public Builder<F> add(Predicate<F> predicate) {
            Objects.requireNonNull(lastField, "Field not set");

            var existing = predicates[lastField.id()];

            if (existing != null) {
                predicates[lastField.id()] = predicate.and(existing);
            } else {
                predicates[lastField.id()] = predicate;
            }

            return this;
        }

        public LogQuery build() {
            return new LogQuery(predicates, schema, queryRange, maxResults);
        }
    }
}