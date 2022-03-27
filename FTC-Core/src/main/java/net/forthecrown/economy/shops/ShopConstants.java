package net.forthecrown.economy.shops;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;

public interface ShopConstants {
    NamespacedKey
            LEGACY_SHOP_KEY     = new NamespacedKey(Main.OLD_NAMESPACE, "signshop"),
            SHOP_KEY            = new NamespacedKey(Crown.inst(), "signshop");

    int     EXAMPLE_ITEM_SLOT   = 2;

    String
            BUY_LABEL           = "=[Buy]=",
            SELL_LABEL          = "=[Sell]=";
    Style
            OUT_OF_STOCK_STYLE  = Style.style(NamedTextColor.RED, TextDecoration.BOLD),
            NORMAL_STYLE        = Style.style(NamedTextColor.GREEN, TextDecoration.BOLD),
            ADMIN_STYLE         = Style.style(NamedTextColor.AQUA, TextDecoration.BOLD);

    Component PRICE_LINE        = Component.text("Price: ").color(NamedTextColor.DARK_GRAY);
}