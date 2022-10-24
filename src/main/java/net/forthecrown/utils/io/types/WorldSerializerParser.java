package net.forthecrown.utils.io.types;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.forthecrown.grenadier.types.WorldArgument;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class WorldSerializerParser implements SerializerParser<World> {
    @Override
    public @NotNull String asString(@NotNull World value) {
        return value.getName();
    }

    @Override
    public <V> V serialize(DynamicOps<V> ops, World value) {
        return ops.createString(value.getName());
    }

    @Override
    public <V> DataResult<World> deserialize(DynamicOps<V> ops, V element) {
        return ops.getStringValue(element)
                .map(Bukkit::getWorld);
    }

    @Override
    public @NotNull ArgumentType<World> getArgumentType() {
        return WorldArgument.world();
    }
}