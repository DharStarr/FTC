package net.forthecrown.guilds.unlockables.nameformat;

import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.events.PotionEffectListener;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildNameFormat;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.unlockables.Unlockable;
import net.forthecrown.guilds.unlockables.Upgradable;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;
import static net.forthecrown.utils.text.Text.nonItalic;
import static net.kyori.adventure.text.Component.text;

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
        return MenuNode.builder()
                .setItem((user, context) -> {
                    Guild guild = context.getOrThrow(GUILD);

                    var item = ItemStacks.builder(Material.MOJANG_BANNER_PATTERN)
                            .setName(bracket.getPreview(
                                    guild.getName(),
                                    guild.getSettings().getPrimaryColor().getTextColor(),
                                    guild.getSettings().getSecondaryColor().getTextColor()))
                            .setFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS)
                            .build();

                    ItemMeta meta = item.getItemMeta();
                    List<Component> lore = meta.lore();

                    if (lore == null) {
                        lore = new ArrayList<>();
                    }

                    if (isUnlocked(guild)) {
                        // Add click-to-active or is-active line
                        lore.add(guild.getSettings().getNameFormat().getBracket() == bracket
                                ? text("Active")
                                        .color(NamedTextColor.YELLOW)
                                        .decoration(TextDecoration.ITALIC, false)
                                : text("Click to activate")
                                        .color(NamedTextColor.GRAY)
                                        .decoration(TextDecoration.ITALIC, false)
                        );
                    } else {
                        var style = nonItalic(NamedTextColor.GRAY);

                        // Add progress lore lines
                        lore.add(getProgressComponent(guild).style(style));
                        lore.add(Component.empty());
                        lore.add(getClickComponent().style(style));
                        lore.add(getShiftClickComponent().style(style));
                    }

                    meta.lore(lore);
                    item.setItemMeta(meta);

                    return item;
                })

                .setRunnable((user, context, c) -> onClick(user, c, context, () -> {
                    Guild guild = context.getOrThrow(GUILD);

                    // If not active
                    if (guild.getSettings().getNameFormat().getBracket() != bracket) {
                            guild.getSettings().getNameFormat().setBracket(bracket);
                    }
                }))

                .build();
    }
}
