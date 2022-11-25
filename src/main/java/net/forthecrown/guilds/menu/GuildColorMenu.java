package net.forthecrown.guilds.menu;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.guilds.unlockables.UnlockableColor;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.context.ClickContext;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.forthecrown.guilds.menu.GuildMenus.PRIMARY_COLOR;

public class GuildColorMenu extends MenuPage {

    private final String title;

    public GuildColorMenu(MenuPage parent, String title) {
        super(parent);
        this.title = title;

        initMenu(Menus.builder(Menus.MAX_INV_SIZE, title), true);
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        UpgradesMenu.addAll(UnlockableColor.values(), builder);
    }

    @Override
    protected MenuNode createHeader() {
        return this;
    }

    @Override
    public @Nullable ItemStack createItem(@NotNull User user, @NotNull InventoryContext context) {
        return ItemStacks.builder(user.getGuild().getSettings().getPrimaryColor().toWool())
                .setName("&e" + title)
                .addLore("&7The " + title + " used in the guild's name format.")
                .build();
    }

    @Override
    public void onClick(User user, InventoryContext context, ClickContext click)
            throws CommandSyntaxException
    {
        super.onClick(user, context, click);
        context.set(PRIMARY_COLOR, title.contains("Primary"));
    }
}