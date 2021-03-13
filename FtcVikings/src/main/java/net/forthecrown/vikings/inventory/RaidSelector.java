package net.forthecrown.vikings.inventory;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.raids.RaidHandler;
import net.forthecrown.vikings.raids.VikingRaid;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nonnull;

public class RaidSelector implements InventoryHolder, Listener {

    private final CrownUser user;
    private Inventory inv;

    public RaidSelector(CrownUser user){
        this.user = user;

        initInv();
        Vikings.getInstance().getServer().getPluginManager().registerEvents(this, Vikings.getInstance());
    }

    private void initInv(){
        inv = Bukkit.createInventory(this, 54, "Raid Selection");

        final ItemStack borderItem = CrownUtils.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "-");
        for (int i = 0; i < inv.getSize(); i++){
            if(i == 4) i++;
            if(i == 10 || i == 19 || i == 28 || i == 37) i += 7;
            inv.setItem(i, borderItem);
        }

        inv.setItem(4, CrownUtils.makeItem(Material.IRON_AXE, 1, true, "Raid selection", "&bChoose a location to raid!"));

        int index = 11;
        for (VikingRaid r: Vikings.getRaidHandler().getRaids()){
            inv.setItem(index, getRaidItem(r));
            index++;
        }
    }

    private ItemStack getRaidItem(VikingRaid raid){
        ItemStack item = CrownUtils.makeItem(Material.PAPER, 1, true, raid.getName());

        if(user.getDataContainer().get(Vikings.getInstance()).getStringList("CompletedRaids").contains(raid.getName()))
            item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);

        return item;
    }

    @Override
    @Nonnull
    public Inventory getInventory() {
        return inv;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if(!(event.getView().getTopInventory().getHolder() instanceof RaidSelector)) return;
        if(event.getCurrentItem() == null) return;
        if(event.getClickedInventory() instanceof PlayerInventory) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if(!event.getCurrentItem().getItemMeta().hasDisplayName()) return;

        VikingRaid raid = RaidHandler.fromName(clickedItem.getItemMeta().getDisplayName());
        if(raid == null) return;

        Vikings.getRaidHandler().callRaid((Player) event.getWhoClicked(), raid);
        event.getWhoClicked().sendMessage("Starting raid!");
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if(!(event.getInventory().getHolder() instanceof RaidSelector)) return;
        HandlerList.unregisterAll(this);
    }
}
