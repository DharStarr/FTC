package net.forthecrown.events;

import net.forthecrown.core.ComVars;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.RoyalWeapons;
import net.forthecrown.inventory.weapon.abilities.WeaponAbility;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class WeaponListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player)) return;
        if(event.getEntity() instanceof ArmorStand) return;

        Player damager = (Player) event.getDamager();

        consumeSword(
                damager.getInventory().getItemInMainHand(),
                sword -> {
                    if(!ComVars.allowNonOwnerSwords() && !sword.getOwner().equals(damager.getUniqueId())) return;
                    sword.damage(damager, event);
                }
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        consumeSword(
                event.getItem(),

                sword -> {
                    WeaponAbility ability = sword.getAbility();
                    if(ability != null) ability.onAltAttack(event);
                }
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        consumeSword(
                event.getPlayer().getInventory().getItemInMainHand(),
                sword -> {
                    WeaponAbility ability = sword.getAbility();
                    if(ability != null) ability.onEntityAltAttack(event);
                }
        );
    }

    void consumeSword(ItemStack item, Consumer<RoyalSword> swordConsumer) {
        if(!RoyalWeapons.isRoyalSword(item)) return;

        RoyalSword sword = new RoyalSword(item);
        swordConsumer.accept(sword);
    }
}
