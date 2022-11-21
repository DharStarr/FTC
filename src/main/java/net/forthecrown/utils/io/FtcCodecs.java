package net.forthecrown.utils.io;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public @UtilityClass class FtcCodecs {
    public final Codec<UUID> UUID_CODEC = Codec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(UUID.fromString(s));
        } catch (IllegalArgumentException exc) {
            return DataResult.error(exc.getMessage());
        }
    }, UUID::toString);

    /* ----------------------------------------------------------- */

    public final Codec<Long> TIMESTAMP_CODEC = Codec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(JsonUtils.DATE_FORMAT.parse(s).getTime());
        } catch (ParseException exc) {
            return DataResult.error(exc.getMessage());
        }
    }, aLong -> {
        Date date = new Date(aLong);
        return JsonUtils.DATE_FORMAT.format(date);
    });

    /* ----------------------------------------------------------- */

    public final Codec<String> KEY_CODEC = Codec.STRING.comapFlatMap(s -> {
        if (!Keys.isValidKey(s)) {
            return Results.errorResult("Invalid key '%s'", s);
        }

        return DataResult.success(s);
    }, Function.identity());

    /* ----------------------------------------------------------- */

    public final Codec<JsonElement> JSON_CODEC = Codec.of(new Encoder<>() {
        @Override
        public <T> DataResult<T> encode(JsonElement input,
                                        DynamicOps<T> ops,
                                        T prefix
        ) {
            if (ops instanceof JsonOps) {
                return DataResult.success((T) input);
            }

            return DataResult.success(
                    JsonOps.INSTANCE.convertTo(ops, input)
            );
        }
    }, new Decoder<>() {
        @Override
        public <T> DataResult<Pair<JsonElement, T>> decode(DynamicOps<T> ops,
                                                           T input
        ) {
            if (ops instanceof JsonOps) {
                return DataResult.success(
                        (Pair<JsonElement, T>) Pair.of(input, input)
                );
            }

            return DataResult.success(
                    Pair.of(
                            ops.convertTo(JsonOps.INSTANCE, input),
                            input
                    )
            );
        }
    });

    /* ----------------------------------------------------------- */

    public final Codec<Location> LOCATION_CODEC = Codec.of(
            new Encoder<>() {
                @Override
                public <T> DataResult<T> encode(Location input,
                                                DynamicOps<T> ops,
                                                T prefix
                ) {
                    var builder = ops.mapBuilder();

                    if (input.isWorldLoaded()) {
                        builder.add(
                                "world",
                                ops.createString(input.getWorld().getName())
                        );
                    }

                    builder.add("x", ops.createDouble(input.getX()));
                    builder.add("y", ops.createDouble(input.getY()));
                    builder.add("z", ops.createDouble(input.getZ()));

                    if (input.getYaw() != 0F) {
                        builder.add("yaw", ops.createFloat(input.getYaw()));
                    }

                    if (input.getPitch() != 0F) {
                        builder.add("pitch", ops.createFloat(input.getPitch()));
                    }

                    return builder.build(prefix);
                }
            },

            new Decoder<>() {
                @Override
                public <T> DataResult<Pair<Location, T>> decode(DynamicOps<T> ops,
                                                                T input
                ) {
                    Dynamic<T> dynamic = new Dynamic<>(ops, input);
                    return dynamic.get("world")
                            .asString()
                            .flatMap(s -> {
                                World w = Bukkit.getWorld(s);

                                if (w == null) {
                                    return Results.errorResult(
                                            "Unknown world: '%s'", s
                                    );
                                }

                                return DataResult.success(w);
                            })

                            .map(world -> {
                                double x = dynamic.get("x")
                                        .asNumber()
                                        .map(Number::doubleValue)
                                        .getOrThrow(false, s -> {});

                                double y = dynamic.get("y")
                                        .asNumber()
                                        .map(Number::doubleValue)
                                        .getOrThrow(false, s -> {});

                                double z = dynamic.get("z")
                                        .asNumber()
                                        .map(Number::doubleValue)
                                        .getOrThrow(false, s -> {});

                                float yaw = dynamic.get("yaw")
                                        .asNumber(0F)
                                        .floatValue();

                                float pitch = dynamic.get("pitch")
                                        .asNumber(0F)
                                        .floatValue();

                                return new Location(world, x, y, z, yaw, pitch);
                            })

                            .map(location -> Pair.of(location, input));
                }
            }
    );

    /* ----------------------------------------------------------- */

    public <E extends Enum<E>> Codec<E> enumCodec(Class<E> eClass) {
        if (!eClass.isEnum()) {
            throw Util.newException("Class '%s' is not an enum", eClass);
        }

        E[] constants = eClass.getEnumConstants();

        if (constants.length > 16) {
            Map<String, E> map = new HashMap<>();
            for (var e: constants) {
                map.put(e.name(), e);
            }

            return Codec.STRING.comapFlatMap(s -> {
                var result = map.get(s.toUpperCase());

                if (result == null) {
                    return Results.errorResult(
                            "Unknown '%s' constant: '%s'",
                            eClass, s
                    );
                }

                return DataResult.success(result);
            }, e -> e.name().toLowerCase());
        }

        return Codec.STRING.comapFlatMap(s -> {
            s = s.toUpperCase();

            for (var e: constants) {
                if (e.name().equals(s)) {
                    return DataResult.success(e);
                }
            }

            return Results.errorResult(
                    "Unknown '%s' constant: '%s'",
                    eClass, s
            );
        }, e -> e.name().toLowerCase());
    }
}