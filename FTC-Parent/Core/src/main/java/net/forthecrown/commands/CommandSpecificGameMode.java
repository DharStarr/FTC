package net.forthecrown.commands;

import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.enums.FtcGameMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

public class CommandSpecificGameMode extends FtcCommand {
    private final FtcGameMode gameMode;
    public CommandSpecificGameMode(@NotNull String name, Permission permission, FtcGameMode mode, String... aliases) {
        super(name, CrownCore.inst());

        this.aliases = aliases;
        this.gameMode = mode;

        setPermission(permission);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> doStuff(getUserSender(c), c.getSource(), false))

                .then(argument("user", UserType.onlineUser())
                        .requires(s -> s.hasPermission(Permissions.GAMEMODES))

                        .executes(c -> doStuff(
                                UserType.getUser(c, "user"),
                                c.getSource(),
                                true
                        ))
                );
    }

    private int doStuff(CrownUser user, CommandSource source, boolean broadcast){
        user.setGameMode(gameMode);
        user.updateFlying();

        if(broadcast) source.sendAdmin(message(user.nickDisplayName()));
        else user.sendMessage(message(Component.text("own")));

        return 0;
    }

    private Component message(Component plr) {
        return Component.text("Set ")
                .color(NamedTextColor.GRAY)
                .append(plr.color(NamedTextColor.YELLOW))
                .append(Component.text(" gamemode to "))
                .append(gameMode.title().color(NamedTextColor.GOLD));
    }

    public static void init(){
        new CommandSpecificGameMode("survival", Permissions.HELPER, FtcGameMode.SURVIVAL, "gms");
        new CommandSpecificGameMode("creative", Permissions.CORE_ADMIN, FtcGameMode.CREATIVE, "gmc");
        new CommandSpecificGameMode("spectator", Permissions.HELPER, FtcGameMode.SPECTATOR, "gmsp");
        new CommandSpecificGameMode("adventure", Permissions.CORE_ADMIN, FtcGameMode.ADVENTURE, "gma");
    }
}
