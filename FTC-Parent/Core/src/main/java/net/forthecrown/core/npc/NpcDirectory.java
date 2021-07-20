package net.forthecrown.core.npc;

import net.forthecrown.core.CrownCore;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class NpcDirectory {
    public static final NamespacedKey KEY = new NamespacedKey(CrownCore.inst(), "interactable_npc");

    public static void interact(String id, Entity entity, Player player) {
        Key key = FtcUtils.parseKey(id);

        InteractableNPC npc = Registries.NPCS.get(key);
        if(npc == null ) {
            CrownCore.logger().warning("Unknown NPC key: " + key.asString());
            return;
        }

        try {
            npc.run(player, entity);
        } catch (RoyalCommandException e) {
            player.sendMessage(e.getComponentMessage());
        }
    }

    public static void createNpc(Key npcKey, Entity entity) {
        entity.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, npcKey.asString());
    }
}
