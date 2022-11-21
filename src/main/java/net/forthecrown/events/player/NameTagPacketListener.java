package net.forthecrown.events.player;

import io.papermc.paper.adventure.PaperAdventure;
import net.forthecrown.user.Users;
import net.forthecrown.user.packet.PacketCall;
import net.forthecrown.user.packet.PacketHandler;
import net.forthecrown.user.packet.PacketListener;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.VanillaAccess;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class NameTagPacketListener implements PacketListener {
    @PacketHandler(ignoreCancelled = true)
    public void onPlayerNameChange(ClientboundSetEntityDataPacket packet, PacketCall call)
            throws ExecutionException, InterruptedException
    {
        CompletableFuture.runAsync(() -> _onNametagPacket(packet, call), call.getExecutor())
                .get();
    }

    private void _onNametagPacket(ClientboundSetEntityDataPacket packet, PacketCall call) {
        var nms = VanillaAccess.getLevel(call.getPlayer().getWorld())
                .getEntity(packet.getId());

        if (nms == null || !(nms.getBukkitEntity() instanceof Player player)) {
            return;
        }

        var user = Users.getLoadedUser(player.getUniqueId());
        var it = packet.getUnpackedData().listIterator();

        while (it.hasNext()) {
            var next = it.next();

            if (!(next.getValue() instanceof Optional optional)
                    || optional.isEmpty()
                    || !(optional.get() instanceof net.minecraft.network.chat.Component)
            ) {
                continue;
            }

            Component listName = user.listDisplayName(call.getUser().get(Properties.RANKED_NAME_TAGS));
            optional = Optional.of(PaperAdventure.asVanilla(listName));
            EntityDataAccessor accessor = next.getAccessor();

            it.set(new SynchedEntityData.DataItem<>(accessor, optional));
            return;
        }
    }
}