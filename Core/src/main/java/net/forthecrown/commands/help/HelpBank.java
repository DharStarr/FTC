package net.forthecrown.commands.help;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class HelpBank extends FtcCommand {

    public HelpBank(){
        super("bank", Crown.inst());

        setAliases("bankhelp", "helpbank");
        setPermission(Permissions.HELP);
        setDescription("Shows some basic info about the Bank");

        register();
    }

    public static final Component MESSAGE = Component.text()
            .append(Crown.prefix())
            .append(Component.text("Bank info:").color(NamedTextColor.YELLOW))
            .append(Component.newline())

            .append(Component.text("The Bank ").color(NamedTextColor.YELLOW))
            .append(Component.text("can provide you with extra items in your adventure on "))
            .append(Component.text("FTC").color(NamedTextColor.GOLD)).append(Component.text("."))
            .append(Component.newline())
            .append(Component.text("To enter the bank, you need a "))
            .append(Component.text("[Bank Ticket]")
                    .hoverEvent(CrownItems.voteTicket().asHoverEvent())
                    .color(NamedTextColor.AQUA))
            .append(Component.text(" earned by voting for the server with"))
            .append(Component.text(" /vote.")
                    .color(NamedTextColor.GOLD)
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to vote for the server")))
                    .clickEvent(ClickEvent.runCommand("/vote")))
            .append(Component.newline())
            .append(Component.text("Entering "))
            .append(Component.text("the vault ").color(NamedTextColor.YELLOW))
            .append(Component.text("will consume the ticket, allowing you to "))
            .append(Component.text("loot the chests ").color(NamedTextColor.YELLOW))
            .append(Component.text("for a short period of time."))

            .build();

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser u = getUserSender(c);

            u.sendMessage(MESSAGE);
            return 0;
        });
    }
}
