package net.forthecrown.commands.manager;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import org.jetbrains.annotations.NotNull;

/**
 * The class used to create, build and register commands
 * <p>
 * stuff like usage and descriptions are basically worthless and exist
 * because I can't be arsed to remove them from commands that already have them
 * </p>
 */
public abstract class FtcCommand extends AbstractCommand {

    @Getter @Setter
    private String helpListName;

    protected FtcCommand(@NotNull String name) {
        super(name, FTC.getPlugin());

        // unknown command for permission message cuz you
        // don't need to know what kinds of commands we have
        permissionMessage(Messages.UNKNOWN_COMMAND);

        setPermission(Permissions.COMMAND_PREFIX + getName());
        setDescription("An FTC command");

        Commands.BY_NAME.put(getName(), this);
    }

    protected static User getUserSender(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return Users.get(c.getSource().asPlayer());
    }
}