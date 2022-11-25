package net.forthecrown.guilds.unlockables.nameformat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.guilds.GuildNameFormat;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.unlockables.Unlockable;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@RequiredArgsConstructor
@Getter
public enum UnlockableBrackets implements Unlockable {
    BRACKETS2(12, 500, GuildNameFormat.Bracket.ROUND),
    BRACKETS3(13, 500, GuildNameFormat.Bracket.ANGLE),
    BRACKETS4(14, 1000, GuildNameFormat.Bracket.SQUARE_SPECIAL1),
    BRACKETS5(15, 1000, GuildNameFormat.Bracket.SQUARE_SPECIAL2),
    ;

    @Getter
    private final int slot, expRequired;
    private final GuildNameFormat.Bracket bracket;

    @Override
    public GuildPermission getPerm() {
        return GuildPermission.CAN_CHANGE_GUILD_COSMETICS;
    }

    @Override
    public String getKey() {
        return name().toLowerCase();
    }

    @Override
    public Component getName() {
        // todo: guild name with the primary and secondary colors
        return bracket.getPreview("guildname", NamedTextColor.AQUA, NamedTextColor.YELLOW);
    }

    @Override
    public MenuNode toInvOption() {
        return null;
    }
}
