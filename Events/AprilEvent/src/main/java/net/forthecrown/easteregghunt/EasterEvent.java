package net.forthecrown.easteregghunt;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.crownevents.EventTimer;
import net.forthecrown.core.crownevents.types.CrownEvent;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.easteregghunt.events.InEventListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.EnumChatFormat;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.time.Duration;
import java.util.Objects;

public class EasterEvent implements CrownEvent<EasterEntry> {

    private final EasterMain main;
    private final EggSpawner spawner;
    private final IUserTracker tracker;

    public static final Objective CROWN = Objects.requireNonNull(Bukkit.getScoreboardManager().getMainScoreboard().getObjective("crown"));
    public static final Location EVENT_LOCATION = new Location(CrownUtils.WORLD_VOID, -623.5, 106, 266.5, 0, 0);
    public static final Location EXIT_LOCATION = new Location(CrownUtils.WORLD_VOID, 10, 10, 10);
    public static boolean open = true;

    public EasterEntry entry;

    public EasterEvent(EasterMain main, EggSpawner spawner){
        this.main = main;
        this.spawner = spawner;
        this.tracker = EasterMain.tracker();
    }

    @Override
    public void start(Player player) {
        if(!open || !tracker.entryAllowed(player)) return;

        tracker.increment(player);
        open = false;
        doEntryCountdown(player);
    }

    private int initAmount = 0;
    public int initialAmount(){
        return initAmount;
    }

    @Override
    public void end(EasterEntry entry) {
        Score record = CROWN.getScore(entry.player().getName());
        CrownUser user = entry.user();
        if(!entry.timer().wasStopped()) entry.timer().stop();
        IChatBaseComponent scrMessage = new ChatComponentText("Score: " + entry.score()).a(EnumChatFormat.GOLD);

        if(!record.isScoreSet() || record.getScore() < entry.score()){
            record.setScore(entry.score());

            IChatBaseComponent text = new ChatComponentText("New record! ").a(EnumChatFormat.YELLOW)
                    .addSibling(scrMessage);

            user.sendMessage(text, ChatMessageType.GAME_INFO);
            user.sendMessage(text, ChatMessageType.CHAT);
        } else {
            IChatBaseComponent text = new ChatComponentText("Better luck next time! ").a(EnumChatFormat.GRAY)
                    .addSibling(scrMessage);
            user.sendMessage(text, ChatMessageType.GAME_INFO);
            user.sendMessage(text, ChatMessageType.CHAT);
        }

        if(EasterMain.bunny.isAlive()) EasterMain.bunny.kill();
        EasterMain.leaderboard.update();
        spawner.removeAllEggs();
        entry.player().teleport(EXIT_LOCATION);
        HandlerList.unregisterAll(entry.inEventListener());

        this.entry = null;
        open = true;
    }

    @Override
    public void complete(EasterEntry timerEntry) {
        end(timerEntry);
    }

    private int loopID = 0;
    private byte secondOn = 5;
    private void doEntryCountdown(Player player){
        secondOn = 5;

        loopID = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            final boolean shouldStart = secondOn < 1;

            Title title = Title.title(
                    Component.text(shouldStart ? "Go!" : secondOn + "").color(NamedTextColor.YELLOW),
                    Component.text("Get ready!").color(NamedTextColor.GOLD),
                    Title.Times.of(Duration.ofMillis(250), Duration.ofMillis(500), Duration.ofMillis(250))
            );
            player.showTitle(title);

            if(shouldStart){

                InEventListener listener = new InEventListener();
                entry = new EasterEntry(player, listener, new EventTimer(player, plr -> end(entry)));
                listener.entry = entry;
                listener.event = this;
                entry.timer().startTickingDown(1);

                EasterMain.inst.getServer().getPluginManager().registerEvents(entry.inEventListener(), EasterMain.inst);

                player.teleport(EVENT_LOCATION);
                initAmount = spawner.placeEggs();
                Bukkit.getScheduler().cancelTask(loopID);
                return;
            }

            secondOn--;
        }, 0, 20);
    }

}
