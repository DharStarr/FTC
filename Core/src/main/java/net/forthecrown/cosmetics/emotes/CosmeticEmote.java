package net.forthecrown.cosmetics.emotes;

import net.forthecrown.commands.emotes.CommandEmote;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.cosmetics.CosmeticEffect;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class CosmeticEmote implements Predicate<Permissible>, CosmeticEffect {

    private final InventoryPos slot;
    private final CommandEmote command;
    private final String name;
    private final Permission permission;
    private final Component[] description;
    private final Key key;

    CosmeticEmote(int slot, CommandEmote command, String name, @Nullable Permission permission, Component... description) {
        this.slot = InventoryPos.fromSlot(slot);
        this.command = command;
        this.name = name;
        this.permission = permission;
        this.description = description;

        this.key = Crown.coreKey(name.toLowerCase());
    }

    CosmeticEmote(int slot, CommandEmote command, String name, String desc){
        this(slot, command, name, null, ChatUtils.convertString(desc, true));
    }

    public String getCommandText(){
        return '/' + getName();
    }

    public Component commandText(){
        return Component.text(getCommandText());
    }

    @Override
    public InventoryPos getPos() {
        return slot;
    }

    public String getName() {
        return name;
    }

    public CommandEmote getCommand() {
        return command;
    }

    public Component[] getDescription() {
        return description;
    }

    public Permission getPermission() {
        return permission;
    }

    public String getPerm(){
        if(permission == null) return null;
        return getPermission().getName();
    }

    @Override
    public void place(Inventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(test(user) ? Material.ORANGE_DYE : Material.GRAY_DYE)
                .setName(commandText().style(FtcFormatter.nonItalic(NamedTextColor.YELLOW)));

        for (Component c: description){
            builder.addLore(c.style(FtcFormatter.nonItalic(NamedTextColor.GRAY)));
        }

        inventory.setItem(getSlot(), builder.build());
    }

    @Override
    public boolean test(Permissible permissible) {
        if(getPermission() == null) return true;
        return permissible.hasPermission(getPermission());
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CosmeticEmote emote = (CosmeticEmote) o;

        return new EqualsBuilder()
                .append(getSlot(), emote.getSlot())
                .append(getCommand(), emote.getCommand())
                .append(getName(), emote.getName())
                .append(getPermission(), emote.getPermission())
                .append(getDescription(), emote.getDescription())
                .append(key, emote.key)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getSlot())
                .append(getCommand())
                .append(getName())
                .append(getPermission())
                .append(getDescription())
                .append(key)
                .toHashCode();
    }
}