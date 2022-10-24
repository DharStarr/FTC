package net.forthecrown.events.player;

import io.papermc.paper.adventure.PaperAdventure;
import net.forthecrown.user.packet.PacketCall;
import net.forthecrown.user.packet.PacketHandler;
import net.forthecrown.user.packet.PacketListener;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.ChatMessageContent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;

public class ChatPacketListener implements PacketListener {
    @PacketHandler
    public void onChat(ServerboundChatPacket packet, PacketCall call) {
        call.setCancelled(true);

        if (handleIllegalChars(call.getPlayer(), packet.message())) {
            return;
        }

        // Vanilla object
        PlayerChatMessage chatMessage = PlayerChatMessage.system(new ChatMessageContent(
                packet.message(),
                PaperAdventure.asVanilla(
                        Text.renderString(call.getPlayer(), packet.message())
                )
        ));

        call.getPacketListener().chat(
                packet.message(),
                chatMessage,
                !Bukkit.isPrimaryThread()
        );
    }

    @PacketHandler
    public void onChatCommand(ServerboundChatCommandPacket packet, PacketCall call) {
        call.setCancelled(true);

        if (handleIllegalChars(call.getPlayer(), packet.command())) {
            return;
        }

        // Switch to main thread to execute command logic, ironically, this
        // shouldn't be done from a plugin-based executor but from the server's
        // executor
        Tasks.runSync(() -> {
            // Prepend this onto it, or it won't find the command lol
            String command = "/" + packet.command();

            call.getPacketListener()
                    .handleCommand(command);
        });
    }

    /** Copies vanilla behaviour in regard to illegal character handling */
    private boolean handleIllegalChars(Player player, String s) {
        if (ServerGamePacketListenerImpl.isChatMessageIllegal(s)) {
            Tasks.runSync(() -> {
                player.kick(
                        Component.translatable("multiplayer.disconnect.illegal_characters"),
                        PlayerKickEvent.Cause.ILLEGAL_CHARACTERS
                );
            });

            return true;
        }

        return false;
    }
}