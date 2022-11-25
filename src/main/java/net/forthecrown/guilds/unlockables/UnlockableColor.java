package net.forthecrown.guilds.unlockables;

import lombok.Getter;
import net.forthecrown.guilds.GuildColor;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@Getter
public enum UnlockableColor implements Unlockable {
    WHITE(13, GuildColor.WHITE),
    BLACK(20, GuildColor.BLACK),
    GRAY(21, GuildColor.GRAY),
    LIGHT_GRAY(22, GuildColor.LIGHT_GRAY),
    BROWN(23, GuildColor.BROWN),
    PINK(24, GuildColor.PINK),
    CYAN(29, GuildColor.CYAN),
    LIGHT_BLUE(30, GuildColor.LIGHT_BLUE),
    BLUE(31, GuildColor.BLUE),
    PURPLE(32, GuildColor.PURPLE),
    MAGENTA(33, GuildColor.MAGENTA),
    GREEN(38, GuildColor.GREEN),
    LIME(39, GuildColor.LIME),
    YELLOW(40, GuildColor.YELLOW),
    ORANGE(41, GuildColor.ORANGE),
    RED(42, GuildColor.RED),
    ;

    private final int slot, expRequired;
    private final GuildColor color;

    UnlockableColor(int slot, GuildColor color) {
        this.slot = slot;
        this.expRequired = 200;
        this.color = color;
    }

    @Override
    public GuildPermission getPerm() {
        return GuildPermission.CAN_CHANGE_GUILD_COSMETICS;
    }

    @Override
    public String getKey() {
        return "PC_" + color.name();
    }

    @Override
    public Component getName() {
        return Text.format("Primary color: {0}",
                NamedTextColor.YELLOW,
                getColor().toText()
        );
    }

    @Override
    public MenuNode toInvOption() {
        return MenuNode.builder()
                .setItem((user, context) -> {
                    var builder = ItemStacks.builder(color.toWool())
                            .setName(text(color.toText(), color.getTextColor()))
                            .setFlags(ItemFlag.HIDE_ENCHANTS);

                    var guild = context.getOrThrow(GUILD);

                    if (isUnlocked(guild)) {
                        if (guild.getSettings().getPrimaryColor() == color) {
                            builder.addEnchant(Enchantment.BINDING_CURSE, 1);
                            builder.addLore("&6Currently Selected");
                        } else {
                            builder.addLore("&7Click to select");
                        }
                    } else {
                        builder
                                .addLore(getProgressComponent(guild))
                                .addLoreRaw(empty())
                                .addLore(getClickComponent())
                                .addLore(getShiftClickComponent());
                    }

                    return builder.build();
                })

                .setRunnable((user, context, click) -> {
                    onClick(user, click, context, () -> {
                        var guild = context.getOrThrow(GUILD);
                        guild.getSettings().setPrimaryColor(color);
                        guild.sendMessage(
                                Text.format("&f{0, user}&r has changed the guild's primary color to {1}.",
                                        user,
                                        text(color.toText(), color.getTextColor())
                                )
                        );
                    });
                })

                .build();
    }
}