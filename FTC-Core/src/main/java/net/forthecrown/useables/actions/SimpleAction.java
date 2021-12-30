package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public record SimpleAction<T extends UsageActionInstance>(Key key,
                                                          Supplier<T> supplier) implements UsageAction<T> {

    @Override
    public T parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return supplier.get();
    }

    @Override
    public T deserialize(JsonElement element) throws CommandSyntaxException {
        return supplier.get();
    }

    @Override
    public JsonElement serialize(T value) {
        return null;
    }

    @Override
    public @NotNull
    Key key() {
        return key;
    }
}
