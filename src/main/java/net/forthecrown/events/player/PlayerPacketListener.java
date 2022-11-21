package net.forthecrown.events.player;

import net.forthecrown.user.packet.PacketCall;
import net.forthecrown.user.packet.PacketHandler;
import net.forthecrown.user.packet.PacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.world.level.GameType;

public class PlayerPacketListener implements PacketListener {
    @PacketHandler(ignoreCancelled = true)
    public void onGameModePacket(ClientboundPlayerInfoPacket packet, PacketCall call) {
        if (packet.getAction() != ClientboundPlayerInfoPacket.Action.ADD_PLAYER
                && packet.getAction() != ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE
        ) {
            return;
        }

        var it = packet.getEntries().listIterator();

        while (it.hasNext()) {
            var next = it.next();

            if (next.getGameMode() != GameType.SPECTATOR
                    || next.getProfile().getId().equals(call.getPlayer().getUniqueId())
            ) {
                continue;
            }

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
}