package net.forthecrown.core.enums;

import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Parrot;

public enum Pet {
    GRAY_PARROT (Parrot.Variant.GRAY, NamedTextColor.GRAY, null),
    GREEN_PARROT (Parrot.Variant.GREEN, NamedTextColor.GREEN, null),
    BLUE_PARROT (Parrot.Variant.BLUE, NamedTextColor.BLUE, null),
    RED_PARROT (Parrot.Variant.RED, NamedTextColor.RED, "ftc.donator2"),
    AQUA_PARROT (Parrot.Variant.CYAN, NamedTextColor.AQUA, "ftc.donator3");

    private final Parrot.Variant variant;
    private final Component name;
    private final String permission;

    Pet(Parrot.Variant variant, TextColor color, String permission){
        this.variant = variant;
        this.name = Component.text(CrownUtils.normalEnum(this)).color(color);
        this.permission = permission;
    }

    public Parrot.Variant getVariant() {
        return variant;
    }

    public Component getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public boolean testPermission(CommandSender sender){
        if(permission == null) return true;
        return sender.hasPermission(permission);
    }
}
