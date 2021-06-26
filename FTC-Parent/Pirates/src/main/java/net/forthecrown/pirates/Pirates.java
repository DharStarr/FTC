package net.forthecrown.pirates;

import net.forthecrown.core.CrownCore;
import net.forthecrown.commands.CommandLeave;
import net.forthecrown.comvars.ComVar;
import net.forthecrown.comvars.ComVars;
import net.forthecrown.comvars.types.ComVarType;
import net.forthecrown.crownevents.ObjectiveLeaderboard;
import net.forthecrown.economy.Balances;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.enums.Rank;
import net.forthecrown.utils.CrownBoundingBox;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.grenadier.RoyalArguments;
import net.forthecrown.grenadier.VanillaArgumentType;
import net.forthecrown.pirates.auctions.Auction;
import net.forthecrown.pirates.auctions.AuctionManager;
import net.forthecrown.pirates.commands.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This is such a mess, holy fuck
 * Half the stuff in this class shouldn't be in this class. Classes were invented for a reason
 */
public final class Pirates extends JavaPlugin implements Listener {

    public File offlineWithParrots;
    public static Pirates inst;
    public GrapplingHook grapplingHook;
    public PirateEvents events;
    public TreasureShulker shulker;

    private static ComVar<Long> auctionExpirationTime;
    private static ComVar<Long> auctionPickUpTime;
    public static ObjectiveLeaderboard leaderboard;
    private static AuctionManager auctionManager;

    public static final Location LEADERBOARD_LOC = new Location(CrownUtils.WORLD, -639.0, 65.5, 3830.5, 90, 0);

    public void onEnable() {
        inst = this;
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // Check yaml
        offlineWithParrots = new File(getDataFolder(), "Offline_With_Parrot_Players.yml");
        if(!offlineWithParrots.exists()){
            try {
                offlineWithParrots.createNewFile();
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(offlineWithParrots);
                yaml.createSection("Players");
                yaml.set("Players", new ArrayList<String>());
                saveYaml(yaml, offlineWithParrots);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //types
        grapplingHook = new GrapplingHook(this);
        auctionManager = new AuctionManager(this);
        events = new PirateEvents(this);
        shulker = new TreasureShulker(this);

        RoyalArguments.register(AuctionArgument.class, VanillaArgumentType.WORD);

        //commands
        new CommandGhTarget();
        new CommandGhShowName();
        new CommandParrot();
        new CommandUpdateLeaderboard();
        new CommandPirate();

        CommandLeave.add(
                new CrownBoundingBox(CrownUtils.WORLD_VOID, -5685, 1, -521, -886, 255, 95),
                new Location(CrownUtils.WORLD_VOID, -800.5, 232, 11.5, -90, 0),
                plr -> {
                    plr.getInventory().clear();
                    return true;
                }
        );

        //events
        getServer().getPluginManager().registerEvents(events, this);
        getServer().getPluginManager().registerEvents(new NpcSmithEvent(), this);

        leaderboard = new ObjectiveLeaderboard(
                "Pirate Points leaderboard",
                Bukkit.getScoreboardManager().getMainScoreboard().getObjective("PiratePoints"),
                LEADERBOARD_LOC
        );

        leaderboard.update();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        auctionExpirationTime = ComVars.set("pr_auctionExpirationTime", ComVarType.LONG, getConfig().getLong("Auctions.ExpirationTime"));
        auctionPickUpTime = ComVars.set("pr_auctionPickupTime", ComVarType.LONG, getConfig().getLong("Auctions.PickUpTime"));

        Calendar cal = Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE);
        if (cal.get(Calendar.DAY_OF_WEEK) != getConfig().getInt("Day")) updateDate();
    }

    public static long getAuctionPickUpTime(){
        return auctionPickUpTime.getValue(259200000L);
    }

    public static long getAuctionExpirationTime(){
        return auctionExpirationTime.getValue(604800000L);
    }

    @SuppressWarnings("deprecation")
    public void onDisable() {
        List<String> players = new ArrayList<>();
        for (UUID playeruuid : events.parrots.values()) {
            try { // Online while reload
                Bukkit.getPlayer(playeruuid).setShoulderEntityLeft(null);
            } catch (Exception e) { // Offline while reload
                players.add(playeruuid.toString());
            }
        }
        events.parrots.clear();
        auctionManager.saveAuctions();

        for (Auction a: AuctionManager.getAuctions().values()){
            a.removeDisplay();
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(offlineWithParrots);
        yaml.set("Players", players);
        saveYaml(yaml, offlineWithParrots);
    }

    public static AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public void updateDate() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            Calendar cal = Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE);
            getConfig().set("Day", cal.get(Calendar.DAY_OF_WEEK));

            ItemStack chosenItem = getRandomHeadFromChest();
            getConfig().set("ChosenHead", ((SkullMeta) chosenItem.getItemMeta()).getPlayerProfile().getName());

            getConfig().set("PlayerWhoSoldHeadAlready", new ArrayList<>());
            getConfig().set("PlayerWhoFoundTreasureAlready", new ArrayList<>());

            shulker.killOld();
            shulker.spawn();

            saveConfig();
        }, 20L);
    }

    void giveTreasure(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);

        double moneyDecider = Math.random();

        int wonAmount;
        int lootBoxKeys;

        if (moneyDecider <= 0.6) {
            wonAmount = 5000;
            lootBoxKeys = 1;
        }
        else if (moneyDecider > 0.6 && moneyDecider <= 0.9) {
            wonAmount = 10000;
            lootBoxKeys = 2;
        }
        else {
            wonAmount = 20000;
            lootBoxKeys = 3;
        }

        CrownCore.getBalances().add(player.getUniqueId(), wonAmount, false);
        Bukkit.dispatchCommand(getServer().getConsoleSender(), "crate givekey " + player.getName() + " lootbox1 " + lootBoxKeys);
        player.sendMessage(
                Component.text("You've found a treasure with ")
                        .color(NamedTextColor.GRAY)
                        .append(Balances.formatted(wonAmount).color(NamedTextColor.YELLOW))
                        .append(Component.text(" inside."))
        );

        List<ItemStack> commonItems = getItems(((Chest) Bukkit.getWorld("world").getBlockAt(
                new Location(Bukkit.getWorld(getConfig().getString("TreasureCommonLoot.world")), getConfig().getInt("TreasureCommonLoot.x"), getConfig().getInt("TreasureCommonLoot.y"), getConfig().getInt("TreasureCommonLoot.z"))).getState()));
        List<ItemStack> rareItems = getItems(((Chest) Bukkit.getWorld("world").getBlockAt(
                new Location(Bukkit.getWorld(getConfig().getString("TreasureRareLoot.world")), getConfig().getInt("TreasureRareLoot.x"), getConfig().getInt("TreasureRareLoot.y"), getConfig().getInt("TreasureRareLoot.z"))).getState()));
        List<ItemStack> specialItems = getItems(((Chest) Bukkit.getWorld("world").getBlockAt(
                new Location(Bukkit.getWorld(getConfig().getString("TreasureSpecialLoot.world")), getConfig().getInt("TreasureSpecialLoot.x"), getConfig().getInt("TreasureSpecialLoot.y"), getConfig().getInt("TreasureSpecialLoot.z"))).getState()));

        for (int i = 0; i < 6; i++) {
            double random = Math.random();
            ItemStack chosenItem;

            if (random <= 0.6) chosenItem = getItemFromList(commonItems);
            else if (random > 0.6 && random <= 0.9) chosenItem = getItemFromList(rareItems);
            else chosenItem = getItemFromList(specialItems);

            player.getInventory().addItem(chosenItem);
        }
        givePP(player, 1);
    }

    public void givePP(Player player, int toadd) {
        Objective pp = getServer().getScoreboardManager().getMainScoreboard().getObjective("PiratePoints");
        Score ppp = pp.getScore(player.getName());
        ppp.setScore(ppp.getScore() + toadd);

        CrownUser user = UserManager.getUser(player);

        if (ppp.getScore() == 1) player.sendMessage(ChatColor.GRAY + "[FTC] You now have " + ChatColor.YELLOW + ppp.getScore() + ChatColor.GRAY + " Pirate point.");
        else {
            player.sendMessage(ChatColor.GRAY+ "You now have " + ChatColor.YELLOW + ppp.getScore() + ChatColor.GRAY + " Pirate points.");

            // Check for sailor / pirate
            if (ppp.getScore() >= 10 && !user.hasRank(Rank.SAILOR)) {

                user.addRank(Rank.SAILOR);
                Bukkit.dispatchCommand(getServer().getConsoleSender(), "lp user " + player.getName() + " parent add free-rank");

                player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);

                for (int i = 0; i <= 5; i++) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().getX(), player.getLocation().getY()+2, player.getLocation().getZ(), 30, 0.2d, 0.1d, 0.2d, 0.275d), i*5L);
                }
                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[FTC] " + ChatColor.WHITE + "You've been promoted to " + ChatColor.DARK_GRAY + ChatColor.BOLD + "{" + ChatColor.GRAY + "Sailor" + ChatColor.DARK_GRAY + ChatColor.BOLD + "}" + ChatColor.WHITE + " !");
                player.sendMessage(ChatColor.WHITE + "You can now select the tag in " + ChatColor.YELLOW + "/rank" + ChatColor.WHITE + " now.");
            }
            else if (ppp.getScore() >= 50 && !user.hasRank(Rank.PIRATE)) {

                user.addRank(Rank.PIRATE);

                player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);

                for (int i = 0; i <= 5; i++) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().getX(), player.getLocation().getY()+2, player.getLocation().getZ(), 30, 0.2d, 0.1d, 0.2d, 0.275d), i*5L);
                }
                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[FTC] " + ChatColor.WHITE + "You've been promoted to " + ChatColor.DARK_GRAY + ChatColor.BOLD + "{" + ChatColor.GRAY + "Pirate" + ChatColor.DARK_GRAY + ChatColor.BOLD + "}" + ChatColor.WHITE + " !");
                player.sendMessage(ChatColor.WHITE + "You can now select the tag in " + ChatColor.YELLOW + "/rank" + ChatColor.WHITE + " now.");
            }
        }
    }

    private List<ItemStack> getItems(Chest chest) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack item : chest.getInventory().getContents()) {
            if (item != null) result.add(item);
        }
        return result;
    }

    private ItemStack getItemFromList(List<ItemStack> list) {
        ItemStack result;

        int index = CrownUtils.getRandomNumberInRange(0, list.size()-1);
        result = list.get(index);
        int count = 0;
        while (list.contains(result) && (count++ != list.size())) {
            result = list.get(++index % list.size());
        }

        return result;
    }

    public ItemStack getRandomHeadFromChest() {
        Location[] chestLocs = {
                getloc("HeadChestLocation1"),
                getloc("HeadChestLocation2"),
                getloc("HeadChestLocation3"),
                getloc("HeadChestLocation4")
        };

        for (Location l: chestLocs){
            if(l.getBlock().getType() != Material.CHEST) throw new NullPointerException(l.toString() + " is not a chest");
        }

        CrownRandom random = new CrownRandom();

        int slot = random.intInRange(0, 26);
        Location chosenLoc = chestLocs[random.intInRange(0, 3)];

        ItemStack chosenItem = ((Chest) Bukkit.getWorld("world").getBlockAt(chosenLoc).getState()).getInventory().getContents()[slot];
        return Objects.requireNonNullElseGet(chosenItem, () -> new ItemStack(Material.STONE));
    }

    private Location getloc(String section) {
        return new Location(Bukkit.getWorld(getConfig().getString(section + ".world")), getConfig().getInt(section + ".x"), getConfig().getInt(section + ".y"), getConfig().getInt(section + ".z"));
    }

    public void giveReward(Player player) {
        CrownCore.getBalances().add(player.getUniqueId(), 10000, false);
        player.sendMessage(ChatColor.GRAY + "You've received " + ChatColor.GOLD + "10,000 rhines" + ChatColor.GRAY + " from " + ChatColor.YELLOW + "Wilhelm" + ChatColor.GRAY + ".");
        givePP(player, 2);
    }

    public boolean checkIfInvContainsHead(PlayerInventory inv) {
        int size = 36;

        for (int i = 0; i < size; i++) {
            ItemStack invItem = inv.getItem(i);
            if (invItem == null) continue;
            if (invItem.getType() != Material.PLAYER_HEAD) continue;
            if(!invItem.hasItemMeta()) continue;

            SkullMeta meta = (SkullMeta) invItem.getItemMeta();
            if(meta.getOwningPlayer() == null) continue;

            if (invItem.hasItemMeta() && ((SkullMeta) invItem.getItemMeta()).getOwner().equalsIgnoreCase(getConfig().getString("ChosenHead"))) {
                invItem.setAmount(invItem.getAmount()-1);
                return true;
            }
        }

        return false;
    }

    public List<String> getTopPlayers(Objective objective, int top) {
        List<String> unsortedResult = new ArrayList<>();
        int score;
        for(String name : objective.getScoreboard().getEntries()) {
            if (unsortedResult.size() < top) {
                unsortedResult.add(name);
            }
            else {
                score = objective.getScore(name).getScore();
                for (String nameInList : unsortedResult) {
                    if (score > objective.getScore(nameInList).getScore()) {
                        String lowestPlayer = nameInList;
                        for (String temp : unsortedResult) {
                            if (objective.getScore(temp).getScore() < objective.getScore(lowestPlayer).getScore()) {
                                lowestPlayer = temp;
                            }
                        }
                        unsortedResult.remove(lowestPlayer);
                        unsortedResult.add(name);
                        break;
                    }
                }
            }
        }

        List<String> sortedResult = new ArrayList<>();

        String playername = null;
        int size = unsortedResult.size();
        for (int j = 1; j <= size; j++) {
            int max = Integer.MIN_VALUE;


            // Zoek max in result
            for (String s : unsortedResult) {
                if (objective.getScore(s).getScore() > max) {
                    max = objective.getScore(s).getScore();
                    playername = s;
                }
            }

            unsortedResult.remove(playername);
            /*if (objective.getScore(playername).getScore() != 0) */sortedResult.add(j + ". " + ChatColor.YELLOW + playername + ChatColor.WHITE + " - " + objective.getScore(playername).getScore());
        }

        return sortedResult;
    }

    public void saveYaml(FileConfiguration yaml, File file) {
        try {
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}