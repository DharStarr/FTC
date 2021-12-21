package net.forthecrown.commands.marriage;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.actions.ActionFactory;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandDivorce extends FtcCommand {

    public CommandDivorce() {
        super("divorce", Crown.inst());

        setPermission(Permissions.MARRY);
        setDescription("Divorce your spouse");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /divorce
     *
     * Permissions used:
     * ftc.marry
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    UserInteractions inter = user.getInteractions();

                    if(inter.getSpouse() == null) throw FtcExceptionProvider.notMarried();
                    if(!inter.canChangeMarriageStatus()) throw FtcExceptionProvider.cannotChangeMarriageStatus();

                    CrownUser spouse = UserManager.getUser(inter.getSpouse());
                    if(!spouse.getInteractions().canChangeMarriageStatus()) throw FtcExceptionProvider.cannotChangeMarriageStatusTarget(spouse);

                    user.sendMessage(
                            Component.translatable("marriage.divorce.confirm",
                                    spouse.nickDisplayName().color(NamedTextColor.YELLOW)
                            )
                                    .color(NamedTextColor.GRAY)
                                    .append(Component.space())
                                    .append(
                                            Component.translatable("buttons.confirm")
                                                    .color(NamedTextColor.AQUA)
                                                    .hoverEvent(Component.text("Click to confirm"))
                                                    .clickEvent(ClickEvent.runCommand("/divorce confirm"))
                                    )
                    );
                    return 0;
                })

                .then(literal("confirm")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);

                            ActionFactory.divorce(user, true);
                            return 0;
                        })
                );
    }
}