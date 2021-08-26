package net.forthecrown.commands.manager;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.Balances;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.Pair;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * The class used to create, build and register commands
 * <p>
 * stuff like usage and descriptions are basically worthless and exist
 * because I can't be arsed to remove them from commands that already have them
 * </p>
 */
public abstract class FtcCommand extends AbstractCommand {

    private String helpListName;

    protected FtcCommand(@NotNull String name, @NotNull Plugin plugin) {
        super(name, plugin);

        setPermissionMessage(ChatColor.WHITE + "Unknown command. Type \"/help\" for help");
        setDescription("An FTC command");
        setPermission(Permissions.COMMAND_PREFIX + name);

        FtcCommands.BY_NAME.put(name, this);
    }

    protected FtcCommand(String name){ this(name, Crown.inst()); }

    protected static Player getPlayerSender(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return c.getSource().asPlayer();
    }

    protected CrownUser getUserSender(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return UserManager.getUser(c.getSource().asPlayer());
    }

    private static Balances bals = Crown.getBalances();
    public static SuggestionProvider<CommandSource> suggestMonies(){
        return (c, b) -> {
            if(!c.getSource().isPlayer()) return Suggestions.empty();
            UUID id = getPlayerSender(c).getUniqueId();

            suggestIf(id, new Pair<>(bals.get(id), new LiteralMessage("Your entire balance")), b);

            suggestIf(id, 1, b);
            suggestIf(id, 10, b);
            suggestIf(id, 100, b);
            suggestIf(id, 1000, b);

            suggestIf(id, 5, b);
            suggestIf(id, 50, b);
            suggestIf(id, 500, b);
            suggestIf(id, 5000, b);

            return b.buildFuture();
        };
    }

    private static void suggestIf(UUID id, int amount, SuggestionsBuilder builder){
        suggestIf(id, new Pair<>(amount, null), builder);
    }

    private static void suggestIf(UUID id, Pair<Integer, Message> pair, SuggestionsBuilder builder){
        int amount = pair.getFirst();
        if(bals.canAfford(id, amount) && (amount + "").toLowerCase().startsWith(builder.getRemaining().toLowerCase())) builder.suggest(amount, pair.getSecond());
    }

    public void setHelpListName(String descriptionName) {
        this.helpListName = descriptionName;
    }

    public String getHelpListName() {
        return helpListName;
    }

    public String getHelpOrNormalName(){
        return FtcUtils.isNullOrBlank(helpListName) ? getName() : getHelpListName();
    }
}