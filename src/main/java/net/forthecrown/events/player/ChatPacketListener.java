package net.forthecrown.events.player;

import io.papermc.paper.adventure.PaperAdventure;
import net.forthecrown.user.packet.PacketCall;
import net.forthecrown.user.packet.PacketHandler;
import net.forthecrown.user.packet.PacketListener;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.ChatMessageContent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerKickEvent;

public class ChatPacketListener implements PacketListener {
    @PacketHandler
    public void onChat(ServerboundChatPacket packet, PacketCall call) {
        call.setCancelled(true);

        if (containsIllegalCharacters(packet.message(), call)) {
            return;
        }

        // Handle player conversing
        if (call.getPlayer().isConversing()) {
            call.getExecutor().execute(() -> {
                call.getPlayer().acceptConversationInput(packet.message());
            });
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

        if (containsIllegalCharacters(packet.command(), call)) {
            return;
        }

        // Switch to main thread to execute command logic
        call.getExecutor().execute(() -> {
            // Prepend this onto it, or it won't find the command lol
            String command = "/" + packet.command();

            call.getPacketListener()
                    .handleCommand(command);
        });
    }

    /** Copies vanilla behaviour in regard to illegal character handling */
    private static boolean containsIllegalCharacters(String s, PacketCall call) {
        var player = call.getPlayer();

        if (ServerGamePacketListenerImpl.isChatMessageIllegal(s)) {
            call.getExecutor().execute(() -> {
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