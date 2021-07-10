package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.grenadier.exceptions.TranslatableExceptionType;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;

public class KeyType implements ArgumentType<Key> {
    private static final KeyType MINECRAFT_INSTANCE = new KeyType(Key.MINECRAFT_NAMESPACE);
    private static final KeyType FTC_INSTANCE = new KeyType(CrownCore.inst().namespace());

    public static final TranslatableExceptionType INVALID = new TranslatableExceptionType("argument.id.invalid");

    private final String defaultNamespace;
    protected KeyType(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    public static KeyType minecraft(){
        return MINECRAFT_INSTANCE;
    }

    public static KeyType ftc(){
        return FTC_INSTANCE;
    }

    public static KeyType key(String defaultNamespace){
        return new KeyType(defaultNamespace);
    }

    @Override
    public Key parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();

        String initial = reader.readUnquotedString();
        if(reader.canRead() && reader.peek() == ':'){
            reader.skip();
            String value = reader.readUnquotedString();

            try {
                return Key.key(initial, value);
            } catch (InvalidKeyException e){
                throw INVALID.createWithContext(GrenadierUtils.correctCursorReader(reader, cursor));
            }
        }

        try {
            return Key.key(defaultNamespace, initial);
        } catch (InvalidKeyException e){
            throw INVALID.createWithContext(GrenadierUtils.correctCursorReader(reader, cursor));
        }
    }
}
