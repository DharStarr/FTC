package net.forthecrown.events.player;

import net.forthecrown.core.*;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.core.Messages;
import net.forthecrown.useables.Usables;
import net.forthecrown.useables.command.Kit;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.user.packet.PacketListeners;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            UserManager.get().getUserLookup().createEntry(event.getPlayer());
        }

        User user = Users.get(event.getPlayer());
        boolean nameChanged = user.onJoin();

        PacketListeners.inject(event.getPlayer());

        if (!event.getPlayer().hasPlayedBefore()) {
            event.getPlayer().teleport(GeneralConfig.getServerSpawn());

            event.joinMessage(Messages.firstJoin(user));

            user.setTimeToNow(TimeField.FIRST_JOIN);

            // Give royal sword
            ItemStack sword = ExtendedItems.ROYAL_SWORD.createItem(user.getUniqueId());
            user.getInventory().addItem(sword);

            //Give join kit
            Kit kit = Usables.get().getKits().get(GeneralConfig.onFirstJoinKit);

            if (kit != null) {
                kit.interact(user.getPlayer());
            }
        } else {
            user.sendMessage(Messages.WELCOME_BACK);

            if (user.get(Properties.VANISHED)) {
                event.joinMessage(null);
            } else {
                event.joinMessage(nameChanged ? Messages.newNameJoinMessage(user) : Messages.joinMessage(user));
            }
        }

        AfkKicker.addOrDelay(user.getUniqueId());
    }
}