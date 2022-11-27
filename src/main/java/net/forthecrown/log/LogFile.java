package net.forthecrown.log;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;
import net.forthecrown.core.FTC;
import net.forthecrown.utils.ArrayIterator;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A log file is a binary representation of the log data of a single day.
 * <p>
 * The logger file is split between 2 major parts: The header, and the content.
 * The header details where in the file a log section is located and what
 * sections are in a file.
 * <p>
 * The header begins with an integer size, stating how many sections the file
 * contains. More specifically, this data is stored in UTF-8, <code>long</code>
 * pairs. The long value is an offset of the section's beginning byte address
 * from the end of the header
 * <p>
 * Sections are lists of {@link BinaryJson} data that begin with a size integer
 * stating how many entries there are. Individual entries are then deserialized
 * and translated into entries by {@link LogSchema} instances.
 */
public class LogFile {
    private static final Logger LOGGER = FTC.getLogger();

    private final DataLog[] logs;

    /** Array of section keys, Contains {@link #count} number of entries */
    private final String[] keys;

    /** Array of section data, Contains {@link #count} number of entries */
    private final ByteArrayOutputStream[] logData;

    /** The amount of written logs written to the {@link #logData} array */
    private int count;

    public LogFile(DataLog[] logs) {
        this.logs = logs;
        this.keys = new String[logs.length];
        this.logData = new ByteArrayOutputStream[logs.length];
    }

    public void fillArrays() throws IOException {
        // Iterator skips over null entries in the log array
        var it = ArrayIterator.unmodifiable(logs);

        int index = 0;
        while (it.hasNext()) {
            DataLog log = it.next();

            if (log.isEmpty()) {
                continue;
            }

            var keyOptional = DataLogs.SCHEMAS.getKey(log.getSchema());

            if (keyOptional.isEmpty()) {
                LOGGER.warn("Unregisterd schema found! Cannot serialize");
                continue;
            }

            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            DataOutputStream dataOutput = new DataOutputStream(byteArray);

            keys[index] = keyOptional.get();
            logData[index++] = byteArray;
            count = index;

            dataOutput.writeInt(log.size());

            for (var entry: log.getEntries()) {
                var json = log.getSchema()
                        .serialize(JsonOps.INSTANCE, entry)
                        .resultOrPartial(LOGGER::error);

                if (json.isEmpty()) {
                    continue;
                }

                JsonElement serialized = json.get();
                BinaryJson.write(serialized, dataOutput);
            }
        }
    }

    public void write(Path path) throws IOException {
        OutputStream stream = Files.newOutputStream(path);
        DataOutputStream output = new DataOutputStream(stream);
        long offset = 0;

        output.writeInt(count);

        // Create header
        for (int i = 0; i < count; i++) {
            String key = keys[i];

            if (Strings.isNullOrEmpty(key)) {
                break;
            }

            var byteOutput = logData[i];

            output.writeUTF(key);
            output.writeLong(offset);

            // Accumulate offset, offset begins after header ends
            offset += byteOutput.size();
        }

        for (int i = 0; i < count; i++) {
            var arr = logData[i];
            arr.writeTo(stream);
        }

        output.close();
        stream.close();
    }

    public static ObjectLongPair<String>[] readHeader(DataInput input)
            throws IOException
    {
        int size = input.readInt();
        ObjectLongPair<String>[] result = new ObjectLongPair[size];

        for (int i = 0; i < size; i++) {
            String key = input.readUTF();
            long address = input.readLong();

            result[i]= ObjectLongPair.of(key, address);
        }

        return result;
    }

    public static void readQuery(DataInput input,
                                 LogSchema schema,
                                 QueryResultBuilder builder
    ) throws IOException {
        int size = input.readInt();

        for (int i = 0; i < size; i++) {
            JsonElement element = BinaryJson.read(input);
            var entryOpt = schema
                    .deserialize(
                            new Dynamic<>(JsonOps.INSTANCE, element)
                    )
                    .resultOrPartial(LOGGER::error);

            if (entryOpt.isEmpty()) {
                continue;
            }

            var entry = entryOpt.get();

            if (builder.getQuery().test(entry)) {
                builder.accept(entry);

                if (builder.getFound() >= builder.getQuery().getMaxResults()) {
                    return;
                }
            }
        }
    }

    public static void readLog(DataInput input, LogContainer container)
            throws IOException
    {
        ObjectLongPair<String>[] header = readHeader(input);

        for (ObjectLongPair<String> pair : header) {
            var schemaOpt = DataLogs.SCHEMAS.getHolder(pair.left());

            if (schemaOpt.isEmpty()) {
                LOGGER.warn("Unknown schema found: '{}' Skipping...",
                        pair.left()
                );
                continue;
            }

            var schema = schemaOpt.get();
            int size = input.readInt();
            DataLog log = new DataLog(schema.getValue());

            for (int j = 0; j < size; j++) {
                JsonElement element = BinaryJson.read(input);
                var entryOpt = schema.getValue()
                        .deserialize(new Dynamic<>(JsonOps.INSTANCE, element))
                        .resultOrPartial(LOGGER::error);

                if (entryOpt.isEmpty()) {
                    continue;
                }

                log.add(entryOpt.get());
            }

            container.setLog(schema, log);
        }
    }
}