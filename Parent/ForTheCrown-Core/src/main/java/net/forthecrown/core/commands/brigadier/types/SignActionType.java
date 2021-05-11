package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.types.interactable.InteractionAction;
import net.forthecrown.core.types.interactable.UseablesManager;
import net.forthecrown.grenadier.CommandSource;

import java.util.concurrent.CompletableFuture;

public class SignActionType implements ArgumentType<InteractionAction> {
    private static final SignActionType INSTANCE = new SignActionType();
    private SignActionType() {}

    public static DynamicCommandExceptionType UNKNOWN_ACTION = new DynamicCommandExceptionType(o -> () -> "Unknown action: " + o);

    public static SignActionType action(){
        return INSTANCE;
    }

    @Override
    public InteractionAction parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        try {
            return UseablesManager.getAction(name);
        } catch (NullPointerException e){
            reader.setCursor(cursor);
            throw UNKNOWN_ACTION.createWithContext(reader, name);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(builder, UseablesManager.getActions());
    }
}