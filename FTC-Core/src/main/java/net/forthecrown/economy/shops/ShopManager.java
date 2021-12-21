package net.forthecrown.economy.shops;

import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.serializer.ShopSerializer;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.UUID;

/**
 * Manages shops n stuff
 * <p></p>
 * Implementation: {@link FtcShopManager}
 */
public interface ShopManager {
    NamespacedKey SHOP_KEY = new NamespacedKey(Crown.inst(), "signshop");

    String BUY_LABEL = "=[Buy]=";
    String SELL_LABEL = "=[Sell]=";

    Style OUT_OF_STOCK_STYLE = Style.style(NamedTextColor.RED, TextDecoration.BOLD);
    Style NORMAL_STYLE = Style.style(NamedTextColor.GREEN, TextDecoration.BOLD);
    Style ADMIN_STYLE = Style.style(NamedTextColor.AQUA, TextDecoration.BOLD);

    Component PRICE_LINE = Component.text("Price: ")
            .color(NamedTextColor.DARK_GRAY);

    /**
     * Checks whether a block is a preexisting signshop.
     * A null check is also performed in the statement
     * @param block The block to check
     * @return Whether the block is a shop or not
     */
    static boolean isShop(Block block){
        if(block == null) return false;
        if(!(block.getState() instanceof Sign)) return false;
        return ((Sign) block.getState()).getPersistentDataContainer().has(SHOP_KEY, PersistentDataType.BYTE);
    }

    /**
     * Tells a shops owner of their shop's stock being 'bad'
     * <p></p>
     * Bad means different things basesd on shop type:
     * <p></p>
     * For sell shops it means the stock is full and needs emptying
     * <p></p>
     * For buy shops it means the shop is out of stock and needs refilling
     * @param owner The shop's owner
     * @param shop The shop to inform of
     */
    static void informOfStockIssue(CrownUser owner, SignShop shop){
        if(shop.getType().isAdmin()) return;

        //If no good, then no go
        if ((shop.getType() != ShopType.BUY || !shop.getInventory().isEmpty()) && (shop.getType() != ShopType.SELL || !shop.getInventory().isFull())) {
            return;
        }

        Location l = shop.getPosition().toLocation();
        Component specification = Component.translatable("shops." + (shop.getType().isBuyType() ? "out" : "full"));
        Component builder = Component.translatable("shops.stockWarning",
                NamedTextColor.YELLOW,
                FtcFormatter.prettyLocationMessage(l, false),
                specification
        );

        owner.sendMessage(builder);
    }

    /**
     * Gets a shop at the given location
     * @param signShop The location of the sign
     * @return The shop at the location, null if no shop exists at the given location
     */
    default SignShop getShop(Location signShop) {
        return getShop(WorldVec3i.of(signShop));
    }

    /**
     * Gets a shop at the given location
     * @param vec The location of the sign
     * @return The shop at the location, null if no shop exists at the given location
     */
    SignShop getShop(WorldVec3i vec);

    /**
     * Gets a shop from a given name
     * @param name The shop's name
     * @return The shop with the given name
     */
    SignShop getShop(String name);

    /**
     * Creates a sign shop at the given location
     * @param location The shop's location
     * @param shopType The shop's type
     * @param price The shop's starting price
     * @param ownerUUID The UUID of the owner
     * @return The created shop
     */
    default SignShop createSignShop(Location location, ShopType shopType, int price, UUID ownerUUID) {
        return createSignShop(WorldVec3i.of(location), shopType, price, ownerUUID);
    }

    /**
     * Creates a sign shop at the given location
     * @param vec The shop's location
     * @param type The shop's type
     * @param price The shop's starting price
     * @param owner The UUID of the owner
     * @return The created shop
     */
    SignShop createSignShop(WorldVec3i vec, ShopType type, int price, UUID owner);

    /**
     * Gets the "Price: 12345 Rhines" line for the shop's sign with the given amount.
     * @param amount The amount to get the text for
     * @return The created text
     */
    Component getPriceLine(int amount);

    /**
     * Gets the hopper inventory with 1 available slot, used for setting the exampleItem of a shop
     * @return the example inventory
     */
    Inventory getExampleInventory();

    /**
     * Saves all shops
     */
    void save();

    /**
     * Reloads all shops
     */
    void reload();

    /**
     * Adds a shop to the loaded shop list
     * @param shop The shop to add to the list
     */
    void addShop(SignShop shop);

    /**
     * Removes a sign from the loaded shop list
     * @param shop The shop to remove
     */
    void removeShop(SignShop shop);

    /**
     * Clears all loaded shops
     */
    void clearShops();

    /**
     * Gets all currently loaded shops
     * <p>Note: This doesn't include all signshops that exist</p>
     * @return All currently loaded shop
     */
    Collection<SignShop> getShops();

    /**
     * Gets the interaction handler for shops
     * @return The shop interaction handler
     */
    ShopInteractionHandler getInteractionHandler();

    /**
     * Gets the serializer incharge of serializing and deserializing shops
     * @return The shop serializer
     */
    ShopSerializer getSerializer();
}