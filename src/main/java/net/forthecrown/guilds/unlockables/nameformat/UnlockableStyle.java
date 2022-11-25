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
public enum UnlockableStyle implements Unlockable {
    STYLE2(30, 500, GuildNameFormat.Stylee.FATB),
    STYLE3(31, 500, GuildNameFormat.Stylee.ITALIC),
    STYLE4(32, 1000, GuildNameFormat.Stylee.ITALIC_FATB),
    STYLE5(33, 1000, GuildNameFormat.Stylee.FAT_STRIKED_B),
    ;

    @Getter
    private final int slot, expRequired;
    private final GuildNameFormat.Stylee stylee;

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
        return stylee.getPreview("guildname", NamedTextColor.AQUA, NamedTextColor.YELLOW);
    }

    @Override
    public MenuNode toInvOption() {
        return null;
    }
}
