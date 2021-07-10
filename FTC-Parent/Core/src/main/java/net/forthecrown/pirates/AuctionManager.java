package net.forthecrown.pirates;

import net.forthecrown.commands.CommandAuction;
import net.forthecrown.core.chat.Announcer;
import net.forthecrown.core.CrownCore;
import net.forthecrown.economy.auctions.Auction;
import net.forthecrown.economy.auctions.AuctionEvents;
import net.forthecrown.economy.auctions.CrownAuction;
import net.forthecrown.squire.Squire;
import net.forthecrown.utils.Worlds;
import net.forthecrown.utils.math.CrownRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public final class AuctionManager {

    static final Map<String, CrownAuction> AUCTIONS = new HashMap<>(); //names and the auctions

    private static final String LABEL = "=[Auction]=";
    private static final Component LABEL_COMPONENT = Component.text(LABEL).style(Style.style(TextDecoration.BOLD));

    public static final Component UNCLAIMED_LABEL = LABEL_COMPONENT.color(NamedTextColor.DARK_RED);
    public static final Component REGULAR_LABEL = LABEL_COMPONENT.color(NamedTextColor.GREEN);
    public static final Component ADMIN_LABEL = LABEL_COMPONENT.color(NamedTextColor.AQUA);
    public static final Component WAITING_FOR_ITEM_CLAIM_LABEL = LABEL_COMPONENT.color(NamedTextColor.YELLOW);

    public static NamespacedKey AUCTION_KEY;

    public static final CrownRegion AUCTION_AREA = new CrownRegion(Worlds.NORMAL, -657, 49, 3848, -616, 21, 3810);

    public AuctionManager(){
        Bukkit.getPluginManager().registerEvents(new AuctionEvents(), CrownCore.inst());

        AUCTION_KEY = Squire.createPiratesKey("auction");
        new CommandAuction();
    }

    public void loadAuctions(){
        File directory = new File(CrownCore.dataFolder() + File.separator + "auctions");

        if(!directory.exists()) directory.mkdir();
        else {
            for (File f: directory.listFiles()){
                if(!f.getName().contains(".yml")) continue;

                String name = f.getName().replaceAll(".yml", "");
                String[] names = name.split("_");
                Location loc = new Location(Bukkit.getWorld(names[1]), Double.parseDouble(names[2]), Double.parseDouble(names[3]), Double.parseDouble(names[4]));

                Auction a = new CrownAuction(loc);
                Announcer.log(Level.INFO, a.getName() + " loaded");
            }
            Announcer.log(Level.INFO, "All auctions loaded");
        }
    }

    public void saveAuctions(){
        for (Auction a: AUCTIONS.values()){
            a.save();
            Entity s = a.getDisplayEntity();
            if(s != null) s.remove();
        }
    }

    public void removeAuction(Auction auction){
        AUCTIONS.remove(auction.getName());
    }

    public void addAuction(CrownAuction auction){
        AUCTIONS.put(auction.getName(), auction);
    }

    public void reloadAuctions(){
        for (Auction a: AUCTIONS.values()){
            a.reload();
        }
    }

    public static Auction getAuction(Location location){
        for (Auction a: AUCTIONS.values()){
            if(a.getLocation().equals(location)) return a;
        }
        return null;
    }

    public static Auction getAuction(String name){
        if(AUCTIONS.containsKey(name)) return AUCTIONS.get(name);
        return null;
    }

    public static TextComponent getPriceLine(@Nullable Integer bid){
        String price = "NONE";
        if(bid != null && bid != -1) price = bid.toString();
        return Component.text("Top Bid: ")
                .color(NamedTextColor.GRAY)
                .append(Component.text("$" + price).color(NamedTextColor.BLACK));
    }

    public static Set<String> getAuctionNames(){
        return AUCTIONS.keySet();
    }

    public static Map<String, Auction> getAuctions() {
        return new HashMap<>(AUCTIONS);
    }
}
