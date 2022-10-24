package net.forthecrown.utils.io.types;

import com.mojang.brigadier.arguments.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.grenadier.types.ByteArgument;
import net.forthecrown.grenadier.types.ShortArgument;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.bukkit.World;

import java.util.Map;

/**
 * Class which stores {@link SerializerParser} constants for easy access.
 */
public interface SerializerParsers {
    Registry<SerializerParser> TYPE_REGISTRY = Registries.newFreezable();
    Map<Class, SerializerParser> BY_TYPE = new Object2ObjectOpenHashMap<>();

    /* ----------------------------- PRIMITIVES ------------------------------ */

    SerializerParser<Byte> BYTE = register("byte_type",
            new PrimitiveSerializerParser<>(ByteArgument.byteArg()) {
                @Override
                public <V> DataResult<Byte> deserialize(DynamicOps<V> ops, V element) {
                    return ops.getNumberValue(element).map(Number::byteValue);
                }
            },

            Byte.class, Byte.TYPE
    );

    SerializerParser<Boolean> BOOL = register("boolean_type",
            new PrimitiveSerializerParser<>(BoolArgumentType.bool()) {
                @Override
                public <V> DataResult<Boolean> deserialize(DynamicOps<V> ops, V element) {
                    return ops.getBooleanValue(element);
                }
            },

            Boolean.class, Boolean.TYPE
    );

    SerializerParser<Short> SHORT = register("short_type",
            new PrimitiveSerializerParser<>(ShortArgument.shortArg()) {
                @Override
                public <V> DataResult<Short> deserialize(DynamicOps<V> ops, V element) {
                    return ops.getNumberValue(element).map(Number::shortValue);
                }
            },

            Short.class, Short.TYPE
    );

    SerializerParser<Integer> INT = register("integer_type",
            new PrimitiveSerializerParser<>(IntegerArgumentType.integer()) {
                @Override
                public <V> DataResult<Integer> deserialize(DynamicOps<V> ops, V element) {
                    return ops.getNumberValue(element).map(Number::intValue);
                }
            },

            Integer.class, Integer.TYPE
    );

    SerializerParser<Float> FLOAT = register("float_type",
            new PrimitiveSerializerParser<>(FloatArgumentType.floatArg()) {
                @Override
                public <V> DataResult<Float> deserialize(DynamicOps<V> ops, V element) {
                    return ops.getNumberValue(element).map(Number::floatValue);
                }
            },
            Float.class, Float.TYPE
    );

    SerializerParser<Double> DOUBLE = register("double_type",
            new PrimitiveSerializerParser<>(DoubleArgumentType.doubleArg()) {
                @Override
                public <V> DataResult<Double> deserialize(DynamicOps<V> ops, V element) {
                    return ops.getNumberValue(element).map(Number::doubleValue);
                }
            },
            Double.class, Double.TYPE
    );

    SerializerParser<Long> LONG = register("long_type",
            new PrimitiveSerializerParser<>(LongArgumentType.longArg()) {
                @Override
                public <V> DataResult<Long> deserialize(DynamicOps<V> ops, V element) {
                    return ops.getNumberValue(element).map(Number::longValue);
                }
            },
            Long.class, Long.TYPE
    );

    SerializerParser<String> STRING = register("string_type",
            new PrimitiveSerializerParser<>(StringArgumentType.greedyString()) {
                @Override
                public <V> DataResult<String> deserialize(DynamicOps<V> ops, V element) {
                    return ops.getStringValue(element);
                }
            },
            String.class
    );

    /* ----------------------------- COMPLEX TYPES ------------------------------ */

    SerializerParser<World> WORLD = register("world_type",
            new WorldSerializerParser(),
            World.class
    );

    SerializerParser<Component> COMPONENT = register("component_type",
            new TextSerializerParser(),
            Component.class
    );

    SerializerParser<Long> TIME = register("time_interval",
            new PeriodSerializerParser(),
            Void.class
    );

    static void init() {
        TYPE_REGISTRY.freeze();
    }

    private static <T> SerializerParser<T> register(String key, SerializerParser<T> type, Class... types) {
        for (var t: Validate.notEmpty(types)) {
            BY_TYPE.put(t, type);
        }

        return TYPE_REGISTRY.register(key, type).getValue();
    }
}