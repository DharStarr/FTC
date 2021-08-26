package net.forthecrown.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.actions.TeleportRequest;
import net.forthecrown.user.actions.UserActionHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;

import static net.forthecrown.commands.manager.FtcExceptionProvider.*;

public class CommandTpask extends FtcCommand {

    public CommandTpask(){
        super("tpask", Crown.inst());

        setAliases("tpa", "tprequest", "tpr", "etpa", "etpask");
        setDescription("Asks a to teleport to a player.");
        setPermission(Permissions.TPA);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Allows players to teleport to another player by asking them.
     *
     * Valid usages of command:
     * - /tpask <player>
     *
     * Permissions used:
     * - ftc.commands.tpa
     *
     * Main Author: Botul
     * Edit by: Wout
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("player", UserArgument.onlineUser())
                .executes(c -> {
                    CrownUser player = getUserSender(c);
                    CrownUser target = UserArgument.getUser(c, "player");
                    checkPreconditions(player, target, false);

                    player.sendMessage(cancelRequest(target));
                    target.sendMessage(tpaMessage("tpa.request.normal", player));

                    UserActionHandler.handleAction(new TeleportRequest(player, target, false));
                    return 0;
                })
        );
    }

    public static TextComponent cancelRequest(CrownUser user){
        return Component.text()
                .color(NamedTextColor.GOLD)
                .append(Component.translatable("tpa.request.sent", user.nickDisplayName().color(NamedTextColor.YELLOW)))
                .append(Component.space())
                .append(Component.text("[✖]")
                        .color(NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.runCommand("/tpacancel " + user.getName()))
                        .hoverEvent(Component.translatable("tpa.button.cancel"))
                )
                .build();
    }

    public static void checkPreconditions(CrownUser sender, CrownUser to, boolean tpaHere) throws CommandSyntaxException {
        if(sender.equals(to)) throw cannotTpToSelf();

        if(!sender.allowsTPA()) throw senderTpaDisabled();
        if(!to.allowsTPA()) throw targetTpaDisabled(to);

        UserInteractions i = sender.getInteractions();
        UserInteractions iTo = to.getInteractions();
        if(i.getOutgoing(to) != null || iTo.getIncoming(sender) != null) throw requestAlreadySent(to);

        if(!sender.hasPermission(Permissions.WORLD_BYPASS)){
            if(tpaHere){ if(isInvalidWorld(sender.getWorld())) throw cannotTpaHere(); }
            else if(isInvalidWorld(to.getWorld())) throw cannotTpaTo(to);
        }

        if(!tpaHere && !sender.canTeleport()) throw cannotTeleport();
    }

    public static boolean isInvalidWorld(World world){
        String name = world.getName();
        return name.contains("senate")
                || name.contains("void")
                || name.contains("raids")
                || name.contains("event");
    }

    public static TextComponent acceptButton(CrownUser target){
        return Component.text("[✔]")
                .color(NamedTextColor.YELLOW)
                .hoverEvent(Component.translatable("tpa.button.accept"))
                .clickEvent(ClickEvent.runCommand("/tpaccept " + target.getName()));
    }

    public static TextComponent denyButton(CrownUser target){
        return Component.text("[✖]")
                .color(NamedTextColor.GRAY)
                .hoverEvent(Component.translatable("tpa.button.deny"))
                .clickEvent(ClickEvent.runCommand("/tpdeny " + target.getName()));
    }

    public static TranslatableComponent tpaMessage(String key, CrownUser displayName){
        return Component.translatable(key, displayName.nickDisplayName().color(NamedTextColor.YELLOW))
                .color(NamedTextColor.GOLD)
                .append(Component.space())
                .append(acceptButton(displayName))
                .append(Component.space())
                .append(denyButton(displayName));
    }
}