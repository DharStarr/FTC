package net.forthecrown.utils.io.types;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

public interface SerializerParser<T> {

    @NotNull String asString(@NotNull T value);

    default @NotNull Component display(@NotNull T value) {
        return Component.text(asString(value));
    }

    @NotNull
    ArgumentType<T> getArgumentType();

    <V> V serialize(DynamicOps<V> ops, T value);

    <V> DataResult<T> deserialize(DynamicOps<V> ops, V element);

    default JsonElement serializeJson(T value) {
        return serialize(JsonOps.INSTANCE, value);
    }

    default Tag serializeTag(T value) {
        return serialize(NbtOps.INSTANCE, value);
    }

    default T deserializeJson(JsonElement element) {
        return deserialize(JsonOps.INSTANCE, element)
                .result()
                .orElse(null);
    }

    default T deserializeTag(Tag tag) {
        return deserialize(NbtOps.INSTANCE, tag)
                .result()
                .orElse(null);
    }
}