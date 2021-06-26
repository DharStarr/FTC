package net.forthecrown.core;

import net.forthecrown.core.serializer.CrownSerializer;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.utils.Nameable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents the current king or queen on the server
 */
public interface Kingship extends Nameable, CrownSerializer<CrownCore> {
    static Kingship inst(){
        return Main.kingship;
    }

    static Component queenTitle(){
        return Component.text("[")
                .append(Component.text("Queen").color(NamedTextColor.YELLOW))
                .append(Component.text("] "))
                .decorate(TextDecoration.BOLD);
    }

    static String getQueenTitle(){ return ChatUtils.getString(queenTitle()); }

    static Component kingTitle(){
        return Component.text("[")
                .append(Component.text("King").color(NamedTextColor.YELLOW))
                .append(Component.text("] "))
                .decorate(TextDecoration.BOLD);
    }

    static String getKingTitle(){ return ChatUtils.getString(kingTitle()); }

    boolean hasKing();

    UUID getUniqueId();
    void set(@Nullable UUID uuid);

    boolean isFemale();

    void setFemale(boolean female);

    CrownUser getUser();
}