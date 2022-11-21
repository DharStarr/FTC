package net.forthecrown.commands.guild;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.user.Users;

interface GuildProvider {
    GuildProvider SENDERS_GUILD = c -> {
        var user = Users.get(c.getSource().asPlayer());
        var guild = user.getGuild();

        if (guild == null) {
            throw Exceptions.format("You are not in any guilds!");
        }

        return guild;
    };

    Guild get(CommandContext<CommandSource> c) throws CommandSyntaxException;

    static GuildProvider argument(String name) {
        return c -> c.getArgument(name, Guild.class);
    }
}