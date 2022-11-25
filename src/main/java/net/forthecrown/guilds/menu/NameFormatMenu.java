package net.forthecrown.guilds.menu;

import lombok.Getter;
import net.forthecrown.guilds.unlockables.nameformat.UnlockableBrackets;
import net.forthecrown.guilds.unlockables.nameformat.UnlockableColorType;
import net.forthecrown.guilds.unlockables.nameformat.UnlockableStyle;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

@Getter
public class NameFormatMenu extends MenuPage {

    public static final int
            SLOT_BRACKET = 9,
            SLOT_COLOR = 18,
            SLOT_RESULT = 26,
            SLOT_STYLE = 27;

    public NameFormatMenu(MenuPage parent) {
        super(parent);

        initMenu(Menus.builder(45, "Format options"), true);
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        builder.add(SLOT_BRACKET, getInfoPaper("Brackets >"));
        builder.add(SLOT_COLOR, getInfoPaper("Colors >"));
        builder.add(SLOT_STYLE, getInfoPaper("Styles >"));

        UpgradesMenu.addAll(UnlockableBrackets.values(), builder);
        UpgradesMenu.addAll(UnlockableColorType.values(), builder);
        UpgradesMenu.addAll(UnlockableStyle.values(), builder);

        builder.add(SLOT_RESULT, MenuNode.builder()
                .setItem((user, context) -> {
                    var guild = context.getOrThrow(GUILD);

                    return ItemStacks.builder(Material.PAPER)
                            .setName(guild.getSettings().getNameFormat().apply(guild.getName())
                                    .decoration(TextDecoration.ITALIC, false))
                            .build();
                })
                .build());
    }

    private static ItemStack getInfoPaper(String name) {
        return ItemStacks.builder(Material.PAPER)
                .setName(Component.text(name, NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false))
                .build();
    }
}
