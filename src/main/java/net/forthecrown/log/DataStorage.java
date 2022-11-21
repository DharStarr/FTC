package net.forthecrown.log;

import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.commons.io.file.PathUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.Optional;

@Getter
public class DataStorage {
    private static final Logger LOGGER = FTC.getLogger();

    /* ----------------------- FILE NAME FORMATTERS ------------------------- */

    public static final DateTimeFormatter FILENAME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral("_")
            .appendText(ChronoField.DAY_OF_WEEK, TextStyle.FULL_STANDALONE)
            .appendLiteral(".json")
            .toFormatter();

    public final DateTimeFormatter YEAR_MONTH_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR)
            .appendLiteral("_")
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .toFormatter();

    private final Path directory;

    DataStorage(Path directory) {
        this.directory = directory;
    }

    /* ------------------------------ METHODS ------------------------------- */

    public YearMonth findMinLog() {
        try {
            if (PathUtils.isEmptyDirectory(directory)) {
                return YearMonth.now();
            }

            YearMonth minYear = YearMonth.now();

            var stream = Files.newDirectoryStream(directory);

            for (var p: stream) {
                try {
                    YearMonth month = YearMonth.parse(
                            p.getFileName().toString(),
                            YEAR_MONTH_FORMATTER
                    );

                    if (month.getYear() < minYear.getYear()
                            && month.getMonthValue() < minYear.getMonthValue()
                    ) {
                        minYear = month;
                    }
                } catch (DateTimeParseException ignored) {}
            }

            stream.close();
            return minYear;
        } catch (IOException exc) {
            LOGGER.error(exc);
        }

        return YearMonth.now();
    }

    public Path getDirectory(ChronoLocalDate date) {
        return directory.resolve(YEAR_MONTH_FORMATTER.format(date));
    }

    public Path getLogFile(ChronoLocalDate date) {
        return getDirectory(date)
                .resolve(FILENAME_FORMATTER.format(date));
    }

    public void loadLogs(ChronoLocalDate date, LogContainer container) {
        Path path = getLogFile(date);

        SerializationHelper.readJsonFile(path, wrapper -> {
            container.deserialize(
                    new Dynamic<>(JsonOps.INSTANCE, wrapper.getSource())
            );
        });
    }

    public void saveLogs(LocalDate date, LogContainer container) {
        Optional<JsonElement> result = container.serialize(JsonOps.INSTANCE)
                .resultOrPartial(LOGGER::error);

        if (result.isEmpty()) {
            return;
        }

        SerializationHelper.writeJson(getLogFile(date), result.get());
    }
}