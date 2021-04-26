package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnegative;
import java.util.*;

public class CommandBalanceTop extends CrownCommandBuilder {
    public CommandBalanceTop() {
        super("balancetop", FtcCore.getInstance());

        maxPage = Math.round(((float) FtcCore.getBalances().getBalanceMap().size())/10);

        setAliases("baltop", "banktop", "cashtop", "topbals", "ebaltop", "ebalancetop");
        setDescription("Displays all the player's balances in order from biggest to smallest");
        register();
    }

    private final int maxPage;

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Explain what command is supposed to be used for..
     *
     *
     * Valid usages of command:
     * - /baltop
     * - /baltop <page number>
     *
     * Author: Botul
     */

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .executes(c -> { //No args -> show first page
                    try {
                        sendBaltopMessage(c.getSource().getBukkitSender(), 0);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    return 0;
                })
                .then(argument("page", IntegerArgumentType.integer(1, maxPage))
                        .executes(c -> { //Page number given -> show that page
                            Integer soup = c.getArgument("page", Integer.class); //Delicious soup
                            sendBaltopMessage(c.getSource().getBukkitSender(), soup);
                            return 0;
                        })
                );
    }

    //Send the message
    private void sendBaltopMessage(CommandSender sender, @Nonnegative int page){
        List<Component> baltopList = getBaltopList();
        Collections.reverse(baltopList);
        if(page > 0) page--;

        final TextComponent border = Component.text("------").color(NamedTextColor.GRAY).decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.TRUE);
        TextComponent.Builder text = Component.text()
                .append(border)
                .append(Component.text(" Top balances ").color(NamedTextColor.YELLOW))
                .append(border)
                .append(Component.newline());

        for(int i = 0 ; i < 10 ; i++){
            if((page*10) + i >= baltopList.size()) break;
            int index = (page*10) + i;

            text.append(Component.text((index+1) + ") ").color(NamedTextColor.GOLD))
                    .append(baltopList.get(index))
                    .append(Component.newline());
        }
        text.append(border)
                .append(Component.text(" Page " +  (page+1) + "/" + maxPage + " ").color(NamedTextColor.YELLOW))
                .append(border);

        //ngl, now that this is just sending one message that's appended together, there's no weird 1 frame thing where
        // the text gets sent line by line lol. It just comes out as one :D
        sender.sendMessage(text);
    }

    //Gets the formatted list of balances, in descending order
    private List<Component> getBaltopList(){
        Map<UUID, Integer> map = getSortedBalances();
        List<Component> list = new ArrayList<>();

        for(UUID id : getSortedBalances().keySet()){
            OfflinePlayer player = Bukkit.getOfflinePlayer(id);
            if(player == null || player.getName() == null) continue;

            list.add(Component.text()
                    .append(Component.text(player.getName()))
                    .append(Component.text(" - "))
                    .append(Balances.formatted(map.get(id)).color(NamedTextColor.YELLOW))
                    .build());

        }
        return list;
    }

    //Gets a sorted list of balances, descending order
    private Map<UUID, Integer> getSortedBalances(){
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(FtcCore.getBalances().getBalanceMap().entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<UUID, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<UUID, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}