package net.forthecrown.economy.blackmarket.merchants;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.CrownRandom;
import org.bukkit.inventory.Inventory;

public interface BlackMarketMerchant extends JsonSerializable {
    Inventory createInventory(CrownUser user);

    void load(JsonElement element);
    void update(CrownRandom random, byte day);
}
