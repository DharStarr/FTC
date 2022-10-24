package net.forthecrown.core;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.utils.Util;
import org.apache.logging.log4j.message.ParameterizedMessage;

/**
 * Class for interacting with the Discord server using
 * the discord srv plugin
 */
public final class FtcDiscord {
    private FtcDiscord() {}

    public static final String
            // Channel names
            UPDATE_CHANNEL  = "updates",
            GENERAL_CHANNEL = "chat",
            STAFF_CHAT      = "cool-club",
            STAFF_LOG       = "staff-log",

            // Staff Log categories
            C_STAFF         = "Staff",
            C_PUNISH        = "Punishments",
            C_END           = "End",
            C_RW            = "RW",
            C_SERVER        = "Server",
            C_MARKETS       = "Markets";

    public static DiscordSRV getHandle() {
        return DiscordSRV.getPlugin();
    }

    public static boolean isActive() {
        if (!Util.isPluginEnabled("DiscordSRV")) {
            return false;
        }

        return getHandle().isEnabled()
                && getHandle().getJda() != null;
    }

    public static TextChannel getTextChannel(String name) {
        // Bro this fucking method name lmao
        return getHandle().getDestinationTextChannelForGameChannelName(name);
    }

    public static TextChannel updateChannel() {
        return getTextChannel(UPDATE_CHANNEL);
    }

    public static TextChannel staffChat() {
        return getTextChannel(STAFF_CHAT);
    }

    /**
     * Sends a message to the staff log
     * @param cat The category of the message
     * @param msg The message itself. Keep in mind the argument format is the same used by Log4J
     * @param args The message arguments
     */
    public static void staffLog(String cat, String msg, Object... args) {
        if (!isActive() || !GeneralConfig.staffLogEnabled) {
            return;
        }

        TextChannel channel = getTextChannel(STAFF_LOG);
        DiscordUtil.queueMessage(channel,
                String.format("**[%s]** %s",
                        cat,
                        new ParameterizedMessage(msg, args)
                                .getFormattedMessage()
                )
        );
    }
}