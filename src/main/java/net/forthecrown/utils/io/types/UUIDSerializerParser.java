package net.forthecrown.utils.io.types;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.forthecrown.grenadier.types.UUIDArgument;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.stream.IntStream;

public class UUIDSerializerParser implements SerializerParser<UUID> {
    @Override
    public @NotNull String asString(@NotNull UUID value) {
        return value.toString();
    }

    @Override
    public @NotNull ArgumentType<UUID> getArgumentType() {
        return UUIDArgument.uuid();
    }

    @Override
    public <V> V serialize(DynamicOps<V> ops, UUID value) {
        if (ops instanceof NbtOps) {
            return ops.createIntList(
                    IntStream.of(UUIDUtil.uuidToIntArray(value))
            );
        }

        return ops.createString(value.toString());
    }

    @Override
    public <V> DataResult<UUID> deserialize(DynamicOps<V> ops, V element) {
        var intList = ops.getIntStream(element);

        if (intList.error().isEmpty()) {
            return intList.map(stream -> UUIDUtil.uuidFromIntArray(stream.toArray()));
        }

        return ops.getStringValue(element)
                .map(UUID::fromString);
    }
}