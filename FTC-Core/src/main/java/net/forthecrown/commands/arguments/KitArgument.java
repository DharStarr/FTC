package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.useables.kits.Kit;
import net.kyori.adventure.key.Key;

import java.util.concurrent.CompletableFuture;

public class KitArgument implements ArgumentType<Key> {
    public static final KitArgument KIT = new KitArgument();
    private KitArgument() {}

    public static final DynamicCommandExceptionType UNKNOWN_KIT = new DynamicCommandExceptionType(o -> () -> "Unknown kit: " + o);

    public static KitArgument kit(){
        return KIT;
    }

    public static Kit getKit(CommandContext<CommandSource> c, String argument){
        return Crown.getKitManager().get(c.getArgument(argument, Key.class));
    }

    @Override
    public Key parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Key key = Keys.argumentType().parse(reader);

        if(!Crown.getKitManager().contains(key)) throw UNKNOWN_KIT.createWithContext(GrenadierUtils.correctReader(reader, cursor), key);

        return key;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return listSuggestions(context, builder, false);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder, boolean ignoreChecks){
        if(ignoreChecks) return FtcSuggestionProvider.suggestRegistry(builder, Crown.getKitManager());

        try {
            return Crown.getKitManager().getSuggestions((CommandContext<CommandSource>) context, builder);
        } catch (CommandSyntaxException ignored) {}
        return Suggestions.empty();
    }
}
