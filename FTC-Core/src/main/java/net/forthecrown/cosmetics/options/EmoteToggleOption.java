package net.forthecrown.cosmetics.options;

import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public class EmoteToggleOption implements InventoryOption {
    @Override
    public int getSlot() {
        return 31;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        boolean allows = user.allowsEmotes();

        ItemStackBuilder builder = new ItemStackBuilder(allows ? Material.STRUCTURE_VOID : Material.BARRIER)
                .setName(Component.text("Emotes " + (allows ? "Enabled" : "Disabled")).style(FtcFormatter.nonItalic(NamedTextColor.GOLD)))
                .addLore(Component.text("Righ-click to " + (allows ? "disable" : "enable") + " emotes.").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)));

        inventory.setItem(getSlot(), builder.build());
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        context.setReloadInventory(true);
        user.setAllowsEmotes(!user.allowsEmotes());
    }
}