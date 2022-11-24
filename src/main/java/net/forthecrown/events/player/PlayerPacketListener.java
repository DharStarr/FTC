package net.forthecrown.events.player;

import net.forthecrown.user.packet.PacketCall;
import net.forthecrown.user.packet.PacketHandler;
import net.forthecrown.user.packet.PacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.world.level.GameType;

import java.util.Objects;

public class PlayerPacketListener implements PacketListener {
    @PacketHandler(ignoreCancelled = true)
    public void onGameModePacket(ClientboundPlayerInfoPacket packet, PacketCall call) {
        if (packet.getAction() != ClientboundPlayerInfoPacket.Action.ADD_PLAYER
                && packet.getAction() != ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE
        ) {
            return;
        }

        boolean playerAdd = packet.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER;
        var it = packet.getEntries().listIterator();
        var id = call.getPlayer().getUniqueId();

        while (it.hasNext()) {
            var next = it.next();

            if (next.getGameMode() != GameType.SPECTATOR
                    || Objects.equals(next.getProfile().getId(), id)
                    || Objects.equals(next.getProfile().getName(), call.getPlayer().getName())
            ) {
                continue;
            }

            if (!playerAdd) {
                it.remove();
            } else {
                it.set(
                        new ClientboundPlayerInfoPacket.PlayerUpdate(
                                next.getProfile(),
                                next.getLatency(),
                                GameType.DEFAULT_MODE,
                                next.getDisplayName(),
                                next.getProfilePublicKey()
                        )
                );
            }
        }

        if (packet.getEntries().isEmpty()) {
            call.setCancelled(true);
        }
    }
}