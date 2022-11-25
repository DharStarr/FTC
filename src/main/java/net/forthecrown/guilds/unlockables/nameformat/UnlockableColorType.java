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
public enum UnlockableColorType implements Unlockable {
    COLORS2(21, 500, GuildNameFormat.Color.ALTERNATE),
    COLORS3(22, 500, GuildNameFormat.Color.GRADIENT_2COLORS),
    COLORS4(23, 1000, GuildNameFormat.Color.GRADIENT_3COLORS),
    COLORS5(24, 1000, GuildNameFormat.Color.GRADIENT_4COLORS),
    ;

    @Getter
    private final int slot, expRequired;
    private final GuildNameFormat.Color color;

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
        return color.getPreview("guildname", NamedTextColor.AQUA, NamedTextColor.YELLOW);
    }

    @Override
    public MenuNode toInvOption() {
        return null;
    }
}
