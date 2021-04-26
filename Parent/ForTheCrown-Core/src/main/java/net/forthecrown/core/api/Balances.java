package net.forthecrown.core.api;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Represents the ingame balances held by the players
 */
public interface Balances extends CrownSerializer<FtcCore> {

    /**
     * Gets the Balanaces instance
     * @return The Balances instance
     */
    static Balances inst(){
        return FtcCore.getBalances();
    }

    /**
     * Gets a formatted currency messaage
     * @param amount The amount to format
     * @return The message, will look like: "1,000,000 Rhines"
     */
    static String getFormatted(int amount){
        return CrownUtils.decimalizeNumber(amount) + " Rhine" + (amount == 1 ? "" : "s");
    }

    /**
     * Same thing as getFormatted but for components
     * @param amount
     * @return
     */
    static Component formatted(int amount){
        return Component.text(getFormatted(amount));
    }

    /**
     * Gets the map of all balances on the server
     * @return Every person's balance
     */
    Map<UUID, Integer> getBalanceMap();

    /**
     * Sets the balance map
     * @param balanceMap The balance map
     */
    void setBalanceMap(Map<UUID, Integer> balanceMap);

    /**
     * Gets the balance of a player by their UUID
     * @param uuid the UUID of the player
     * @return The balance of the player
     */
    Integer get(UUID uuid);

    /**
     * Sets the players balance
     * @param uuid The UUID of the player
     * @param amount The new balance of the player
     */
    void set(UUID uuid, Integer amount);

    Component withCurrency(UUID id);

    /**
     * Adds to a players balance
     * @param uuid The UUID of the player
     * @param amount The amount of Rhines to add to their balance
     */
    void add(UUID uuid, Integer amount);

    /**
     * Adds to a players balance
     * @param uuid The UUID of the player
     * @param amount The amount to add
     * @param isTaxed Whether the transaction is taxed, aka, if a % doesn't reach the player
     */
    void add(UUID uuid, Integer amount, boolean isTaxed);

    /**
     * Gets the % a player is taxed
     * @param uuid The UUID of the player
     * @return the tax bracket of the person
     */
    Integer getTax(UUID uuid);

    /**
     * Returns a more readable version of a person's balance
     * @param id The ID of the balance to get
     * @return A more readable number, example: 1,000,000 instead of 1000000
     */
    String getDecimalized(UUID id);

    /**
     * Sets a user's balance, ignoring the maximum balance limit
     * @param id The id of the balance to set
     * @param amount The amount to set the balance
     */
    void setUnlimited(UUID id, Integer amount);

    /**
     * Gets a balance string as: "AMOUNT Rhines" or "AMOUNT Rhine" if the amount is 1
     * @param id The ID of the balance to get
     * @return The balance message
     */
    String getWithCurrency(UUID id);
}