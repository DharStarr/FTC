package net.forthecrown.user;

import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.PunishmentEntry;
import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.crownevents.EventTimer;
import net.forthecrown.economy.market.MarketDisplay;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.data.Faction;
import net.forthecrown.user.data.Rank;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Statistic;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.Date;
import java.util.function.Function;

public class ProfilePrinter {
    private final FtcUser user;
    private final boolean self;
    private final boolean adminViewer;
    private final boolean profilePublic;

    private final TextComponent.Builder builder;
    private int headerLength;

    public ProfilePrinter(CrownUser user, CommandSource source) {
        this(
                user,
                source.textName().equalsIgnoreCase(user.getName()),
                source.hasPermission(Permissions.PROFILE_BYPASS)
        );
    }

    public ProfilePrinter(CrownUser user, boolean self, boolean adminViewer) {
        this.user = (FtcUser) user;
        this.self = self;
        this.adminViewer = adminViewer;
        this.profilePublic = user.isProfilePublic();

        builder = Component.text();
    }

    public boolean isViewingAllowed() {
        return self || adminViewer || profilePublic;
    }

    public Component printFull() {
        header();
        optionalInfo();
        basicInfo();

        if(adminViewer) adminInfo();

        footer();
        return printCurrent();
    }

    public ProfilePrinter header() {
        String header = "&6&m----&e " + (self ? "Your" : (user.getNickOrName() + "'s")) + " player profile &6&m----";
        this.headerLength = header.length();

        return append(header);
    }

    public ProfilePrinter footer() {
        newLine();
        return append("-".repeat(headerLength < 1 ? 10 : (int) (headerLength * 0.75)));
    }

    public ProfilePrinter basicInfo() {
        line("Branch", user.getFaction().getName(), user.getFaction() != Faction.DEFAULT);
        line("Rank", user.getRank().noEndSpacePrefix(), user.getRank() != Rank.DEFAULT);

        line("Gems", user.getGems() + "", user.getGems() > 0);
        line("Rhines", Crown.getEconomy().get(user.getUniqueId()) + "");

        return this;
    }

    public ProfilePrinter optionalInfo() {
        line("AFK", user.getAfkReason(), user.isAfk() && user.getAfkReason() != null);

        line("Allowed to swap branches in", timeThing(), !user.canSwapFaction() && (self || adminViewer));
        line("Play time", FtcFormatter.decimalizeNumber(playTime()));

        line("Married to", marriedMessage());

        Objective crown = user.getScoreboard().getObjective("crown");
        Score crownScore = crown.getScore(user.getName());
        line("Crown score",
                Component.text(ComVars.isEventTimed() ? EventTimer.getTimerCounter(crownScore.getScore()).toString() : crownScore.getScore() + ""),
                crownScore.getScore() > 0 && ComVars.isEventActive()
        );

        return this;
    }

    public ProfilePrinter adminInfo() {
        PunishmentManager list = Crown.getPunishmentManager();
        PunishmentEntry entry = list.getEntry(user.getUniqueId());

        Component locMessage = user.getLocation() == null ? null : FtcFormatter.clickableLocationMessage(user.getLocation(), true);
        Component punishmentDisplay = Component.newline().append(entry.display(false));

        Component ignored = user.interactions.blocked.isEmpty() ?
                null :
                Component.text(ListUtils.join(user.interactions.blocked, id -> UserManager.getUser(id).getName()));

        Component separated = user.interactions.separated.isEmpty() ?
                null :
                Component.text(ListUtils.join(user.interactions.separated, id -> UserManager.getUser(id).getName()));

        append("\nAdmin Info:");

        line(" Ranks", ListUtils.join(user.getAvailableRanks(), r -> r.name().toLowerCase()));

        newLine();
        onlineTimeThing();

        line(" IP", user.ip);
        line(" PreviousNames", user.previousNames.isEmpty() ? null : ListUtils.join(user.previousNames, Function.identity()));

        line(" Ignored", ignored);
        line(" Separated", separated);

        line(" OwnedShop",
                user.marketOwnership.currentlyOwnsShop() ?
                        MarketDisplay.infoText(Crown.getMarkets().get(user.getUniqueId())) :
                        null
        );

        line(" MarriageCooldown", marriageCooldown());
        line(" Location", locMessage, user.isOnline());

        append(punishmentDisplay);
        return this;
    }

    public ProfilePrinter newLine() {
        return append(Component.newline());
    }

    public ProfilePrinter append(Component text) {
        if(text != null) builder.append(text);
        return this;
    }

    public ProfilePrinter append(String text) {
        return append(FtcUtils.isNullOrBlank(text) ? null : ChatUtils.convertString(text));
    }

    public ProfilePrinter line(String line, Component text) {
        return line(line, text, text != null);
    }

    public ProfilePrinter line(String line, String text) {
        return line(line, text == null ? null : ChatUtils.convertString(text));
    }

    public ProfilePrinter line(String line, String text, boolean shouldInclude) {
        return line(line, text == null ? null : ChatUtils.convertString(text), shouldInclude);
    }

    public ProfilePrinter line(String line, Component text, boolean shouldInclude) {
        if(!shouldInclude || text == null) return this;

        newLine();
        append(Component.text(line + ": ").color(NamedTextColor.YELLOW));
        return append(text);
    }

    private long playTime() {
        return user.getOfflinePlayer().getStatistic(Statistic.PLAY_ONE_MINUTE)/20/60/60;
    }

    private String timeThing() {
        long timeUntil = user.getNextAllowedBranchSwap() - System.currentTimeMillis();
        return FtcFormatter.convertMillisIntoTime(timeUntil);
    }

    private Component marriedMessage(){
        if(user.getInteractions().getSpouse() == null) return null;

        return user.getInteractions().spouseUser().nickDisplayName();
    }

    private void onlineTimeThing() {
        if(user.isOnline()){
            append(
                    Component.text(" Has been online for ")
                            .color(NamedTextColor.YELLOW)
                            .append(FtcFormatter.millisIntoTime(System.currentTimeMillis() - user.getPlayer().getLastLogin()).color(NamedTextColor.WHITE))
            );
        } else {
            append(
                    Component.text(" Has been offline for ")
                            .color(NamedTextColor.YELLOW)
                            .hoverEvent(Component.text(new Date(user.getOfflinePlayer().getLastLogin()).toString()))
                            .append(FtcFormatter.millisIntoTime(System.currentTimeMillis() - user.getOfflinePlayer().getLastLogin()).color(NamedTextColor.WHITE))
            );
        }
    }

    private Component marriageCooldown() {
        UserInteractions interactions = user.getInteractions();

        if(interactions.canChangeMarriageStatus()) return null;

        long time = interactions.getLastMarriageChange();
        return Component.text(FtcFormatter.getDateFromMillis(time))
                .hoverEvent(Component.text(new Date(time).toString()));
    }

    public Component printCurrent() {
        return builder.build();
    }

    public boolean isAdminViewer() {
        return adminViewer;
    }

    public boolean isProfilePublic() {
        return profilePublic;
    }

    public boolean isSelf() {
        return self;
    }

    public CrownUser getUser() {
        return user;
    }
}
