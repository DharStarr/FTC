package net.forthecrown.core;

import net.forthecrown.comvars.ComVar;
import net.forthecrown.comvars.types.ComVarTypes;
import net.forthecrown.regions.RegionConstants;
import net.forthecrown.utils.TimeUtil;
import net.forthecrown.utils.Worlds;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;

import static net.forthecrown.comvars.ComVarRegistry.set;

public final class ComVars {
    static ComVar<Key>              onFirstJoinKit;

    static ComVar<World>            treasureWorld;
    static ComVar<World>            regionWorld;

    static ComVar<String>           spawnRegion;

    static ComVar<Byte>             maxNickLength;
    static ComVar<Byte>             maxBossDifficulty;
    static ComVar<Byte>             maxTreasureItems;

    static ComVar<Short>            nearRadius;
    static ComVar<Short>            hoppersInOneChunk;
    static ComVar<Short>            maxGuildMembers;

    static ComVar<Long>             marriageCooldown;
    static ComVar<Long>             userDataResetInterval;
    static ComVar<Long>             branchSwapCooldown;
    static ComVar<Long>             auctionExpirationTime;
    static ComVar<Long>             auctionPickupTime;
    static ComVar<Long>             autoSaveIntervalMins;
    static ComVar<Long>             marketOwnershipSafeTime;
    static ComVar<Long>             evictionCleanupTime;
    static ComVar<Long>             voteTime;
    static ComVar<Long>             voteInterval;
    static ComVar<Long>             guildJoinRequirement;
    static ComVar<Long>             marketStatusCooldown;

    static ComVar<Boolean>          allowOtherPlayerNicks;
    static ComVar<Boolean>          taxesEnabled;
    static ComVar<Boolean>          logAdminShop;
    static ComVar<Boolean>          logNormalShop;
    static ComVar<Boolean>          crownEventActive;
    static ComVar<Boolean>          crownEventIsTimed;
    static ComVar<Boolean>          hulkSmashPoles;
    static ComVar<Boolean>          endOpen;

    static ComVar<Integer>          tpTickDelay;
    static ComVar<Integer>          tpCooldown;
    static ComVar<Integer>          tpaExpiryTime;
    static ComVar<Integer>          startRhines;
    static ComVar<Integer>          baronPrice;
    static ComVar<Integer>          chickenLevitation;
    static ComVar<Integer>          chickenLevitationTime;
    static ComVar<Integer>          ghSpecialReward;
    static ComVar<Integer>          ghFinalReward;
    static ComVar<Integer>          maxMoneyAmount;
    static ComVar<Integer>          maxTreasurePrize;
    static ComVar<Integer>          minTreasurePrize;
    static ComVar<Integer>          maxShopEarnings;
    static ComVar<Integer>          maxSignShopPrice;

    private ComVars() {}

    static void reload(Configuration config) {
        onFirstJoinKit = set(           "onFirstJoinKit",           ComVarTypes.KEY,            Key.key(Crown.inst(), config.getString("OnJoinKit")));

        treasureWorld = set(            "treasureWorld",            ComVarTypes.WORLD,          Bukkit.getWorld(config.getString("Pirates.TreasureWorld")));
        regionWorld = set(              "regionWorld",              ComVarTypes.WORLD,          Bukkit.getWorld(config.getString("RegionWorld")));

        spawnRegion = set(              "spawnRegion",              ComVarTypes.STRING,         config.getString("SpawnRegion", RegionConstants.DEFAULT_SPAWN_NAME));

        maxNickLength = set(            "maxNickLength",            ComVarTypes.BYTE,           (byte) config.getInt("MaxNickLength"));
        maxBossDifficulty = set(        "maxBossDifficulty",        ComVarTypes.BYTE,           (byte) config.getInt("MaxBossDifficulty"));
        maxTreasureItems = set(         "maxTreasureItems",         ComVarTypes.BYTE,           (byte) config.getInt("Pirates.MaxTreasureItems"));

        nearRadius = set(               "nearRadius",               ComVarTypes.SHORT,          (short) config.getInt("NearRadius"));
        hoppersInOneChunk = set(        "hoppersInOneChunk",        ComVarTypes.SHORT,          (short) config.getInt("HoppersInOneChunk"));
        maxGuildMembers = set(          "maxGuildMembers",          ComVarTypes.SHORT,          (short) config.getInt("MaxGuildMembers"));

        marriageCooldown = set(         "marriageCooldown",         ComVarTypes.LONG,           config.getLong("MarriageStatusCooldown"));
        userDataResetInterval = set(    "userDataResetInterval",    ComVarTypes.LONG,           config.getLong("UserDataResetInterval"));
        branchSwapCooldown = set(       "branchSwapCooldown",       ComVarTypes.LONG,           config.getLong("BranchSwapCooldown"));
        auctionExpirationTime = set(    "auctionExpirationTime",    ComVarTypes.LONG,           config.getLong("Auctions.ExpirationTime"));
        auctionPickupTime = set(        "auctionPickupTime",        ComVarTypes.LONG,           config.getLong("Auctions.PickUpTime"));
        autoSaveIntervalMins = set(     "autoSaveIntervalMins",     ComVarTypes.LONG,           config.getLong("System.save-interval-mins"));
        marketOwnershipSafeTime = set(  "marketOwnershipSafeTime",  ComVarTypes.LONG,           config.getLong("MarketOwnershipSafeTime"));
        evictionCleanupTime = set(      "evictionCleanupTime",      ComVarTypes.LONG,           config.getLong("MarketEvictionTime"));
        voteTime = set(                 "voteTime",                 ComVarTypes.LONG,           config.getLong("VoteTime"));
        voteInterval = set(             "voteInterval",             ComVarTypes.LONG,           config.getLong("VoteInterval"));
        guildJoinRequirement = set(     "guildJoinRequirement",     ComVarTypes.LONG,           config.getLong("GuildJoinRequirement"));
        marketStatusCooldown = set(     "marketStatusCooldown",     ComVarTypes.LONG,           config.getLong("MarketStatusCooldown"));

        taxesEnabled = set(             "taxesEnabled",             ComVarTypes.BOOLEAN,        config.getBoolean("Taxes"));
        logAdminShop = set(             "logAdminShop",             ComVarTypes.BOOLEAN,        config.getBoolean("Shops.log-admin-purchases"));
        logNormalShop = set(            "logNormalShop",            ComVarTypes.BOOLEAN,        config.getBoolean("Shops.log-normal-purchases"));
        allowOtherPlayerNicks = set(    "allowOtherPlayerNicks",    ComVarTypes.BOOLEAN,        config.getBoolean("AllowOtherPlayerNicks"));
        crownEventActive = set(         "crownEventActive",         ComVarTypes.BOOLEAN,        config.getBoolean("CrownEventActive"));
        crownEventIsTimed = set(        "crownEventIsTimed",        ComVarTypes.BOOLEAN,        config.getBoolean("EventScoreIsTimer"));
        hulkSmashPoles = set(           "hulkSmashPoles",           ComVarTypes.BOOLEAN,        config.getBoolean("HulkSmashPoles"));
        endOpen = set(                  "endOpen",                  ComVarTypes.BOOLEAN,        config.getBoolean("EndOpen"));

        maxMoneyAmount = set(           "maxMoneyAmount",           ComVarTypes.INTEGER,        config.getInt("MaxMoneyAmount"));
        tpTickDelay = set(              "tpTickDelay",              ComVarTypes.INTEGER,        config.getInt("TeleportTickDelay"));
        tpCooldown = set(               "tpCooldown",               ComVarTypes.INTEGER,        config.getInt("TeleportCooldown"));
        tpaExpiryTime = set(            "tpaExpiryTime",            ComVarTypes.INTEGER,        config.getInt("TpaExpiryTime"));
        startRhines = set(              "startRhines",              ComVarTypes.INTEGER,        config.getInt("StartRhines"));
        baronPrice = set(               "baronPrice",               ComVarTypes.INTEGER,        config.getInt("BaronPrice"));
        chickenLevitation = set(        "chickenLevitation",        ComVarTypes.INTEGER,        config.getInt("MiniGameRegion.ChickenLevitation"));
        chickenLevitationTime = set(    "chickenLevitationTime",    ComVarTypes.INTEGER,        config.getInt("MiniGameRegion.ChickenLevitationTime"));
        ghSpecialReward = set(          "ghSpecialReward",          ComVarTypes.INTEGER,        config.getInt("Pirates.SpecialReward"));
        ghFinalReward = set(            "ghFinalReward",            ComVarTypes.INTEGER,        config.getInt("Pirates.FinalReward"));
        maxTreasurePrize = set(         "maxTreasurePrize",         ComVarTypes.INTEGER,        config.getInt("Pirates.MaxTreasurePrize"));
        minTreasurePrize = set(         "minTreasurePrize",         ComVarTypes.INTEGER,        config.getInt("Pirates.MinTreasurePrize"));
        maxShopEarnings = set(          "maxShopEarnings",          ComVarTypes.INTEGER,        config.getInt("MaxShopEarnings"));
        maxSignShopPrice = set(         "maxSignShopPrice",         ComVarTypes.INTEGER,        config.getInt("MaxSignShopPrice"));
    }

    static void save(Configuration config) {
        config.set("OnJoinKit",                             onFirstJoinKit.getValue().value());

        config.set("TreasureWorld",                         treasureWorld.getValue().getName());
        config.set("RegionWorld",                           regionWorld.getValue().getName());

        config.set("SpawnRegion",                           spawnRegion.getValue());

        config.set("MaxNickLength",                         maxNickLength.getValue((byte) 16));
        config.set("MaxBossDifficulty",                     maxBossDifficulty.getValue());
        config.set("Pirates.MaxTreasureItems",              maxTreasureItems.getValue());

        config.set("NearRadius",                            nearRadius.getValue());
        config.set("HoppersInOneChunk",                     hoppersInOneChunk.getValue());
        config.set("MaxGuildMembers",                       maxGuildMembers.getValue());

        config.set("MarriageStatusCooldown",                marriageCooldown.getValue());
        config.set("UserDataResetInterval",                 userDataResetInterval.getValue());
        config.set("BranchSwapCooldown",                    branchSwapCooldown.getValue());
        config.set("Auctions.ExpirationTime",               auctionExpirationTime.getValue());
        config.set("Auctions.PickUpTime",                   auctionPickupTime.getValue());
        config.set("System.save-interval-mins",             autoSaveIntervalMins.getValue());
        config.set("MarketOwnershipSafeTime",               marketOwnershipSafeTime.getValue());
        config.set("evictionCleanupTime",                   evictionCleanupTime.getValue());
        config.set("VoteTime",                              voteTime.getValue());
        config.set("VoteInterval",                          voteInterval.getValue());
        config.set("GuildJoinRequirement",                  guildJoinRequirement.getValue());
        config.set("MarketStatusCooldown",                  marketStatusCooldown.getValue());

        config.set("AllowOtherPlayerNicks",                 allowOtherPlayerNicks.getValue());
        config.set("Taxes",                                 taxesEnabled.getValue());
        config.set("Shops.log-normal-purchases",            logNormalShop.getValue());
        config.set("Shops.log-admin-purchases",             logAdminShop.getValue());
        config.set("CrownEventActive",                      crownEventActive.getValue());
        config.set("EventScoreIsTimer",                     crownEventIsTimed.getValue());
        config.set("HulkSmashPoles",                        hulkSmashPoles.getValue());
        config.set("EndOpen",                               endOpen.getValue());

        config.set("TeleportTickDelay",                     tpTickDelay.getValue());
        config.set("TeleportCooldown",                      tpCooldown.getValue());
        config.set("TpaExpiryTime",                         tpaExpiryTime.getValue());
        config.set("StartRhines",                           startRhines.getValue(100));
        config.set("BaronPrice",                            baronPrice.getValue());
        config.set("MiniGameRegion.ChickenLevitation",      chickenLevitation.getValue());
        config.set("MiniGameRegion.ChickenLevitationTime",  chickenLevitationTime.getValue());
        config.set("Pirates.FinalReward",                   ghFinalReward.getValue());
        config.set("Pirates.SpecialReward",                 ghSpecialReward.getValue());
        config.set("MaxMoneyAmount",                        maxMoneyAmount.getValue());
        config.set("Pirates.MaxTreasurePrize",              maxTreasurePrize.getValue());
        config.set("Pirates.MinTreasurePrize",              minTreasurePrize.getValue());
        config.set("MaxShopEarnings",                       maxShopEarnings.getValue());
        config.set("MaxSignShopPrice",                      maxSignShopPrice.getValue());
    }

    public static String getSpawnRegion() {
        return spawnRegion.getValue(RegionConstants.DEFAULT_SPAWN_NAME);
    }

    public static World getRegionWorld() {
        return regionWorld.getValue(Worlds.OVERWORLD);
    }


    public static byte getMaxBossDifficulty() {
        return maxBossDifficulty.getValue((byte) 5);
    }

    public static byte getMaxTreasureItems() {
        return maxTreasureItems.getValue((byte) 5);
    }

    public static byte getMaxNickLength() {
        return maxNickLength.getValue((byte) 16);
    }




    public static short getHoppersInOneChunk() {
        return hoppersInOneChunk.getValue((short) 256);
    }

    public static short getNearRadius() {
        return nearRadius.getValue((short) 200);
    }

    public static short getMaxGuildMembers() {
        return maxGuildMembers.getValue((short) 5);
    }




    public static long getMarriageCooldown() {
        return marriageCooldown.getValue(259200000L);
    }

    public static long getAuctionExpirationTime() {
        return auctionExpirationTime.getValue(604800000L);
    }

    public static long getAuctionPickupTime() {
        return auctionPickupTime.getValue(259200000L);
    }

    public static long getUserResetInterval() {
        //Default: 2 months
        return userDataResetInterval.getValue(5356800000L);
    }

    public static long getBranchSwapCooldown()  {
        return branchSwapCooldown.getValue(TimeUtil.DAY_IN_MILLIS * 2);
    }

    public static long getShopOwnershipSafeTime() {
        return marketOwnershipSafeTime.getValue(TimeUtil.WEEK_IN_MILLIS * 2);
    }

    public static long getEvictionCleanupTime() {
        return evictionCleanupTime.getValue(TimeUtil.DAY_IN_MILLIS * 3);
    }

    public static long getVoteInterval() {
        return voteInterval.getValue(TimeUtil.DAY_IN_MILLIS * 3);
    }

    public static long getGuildJoinRequirement() {
        return guildJoinRequirement.getValue(TimeUtil.MONTH_IN_MILLIS * 2);
    }

    public static long getMarketStatusCooldown() {
        return marketStatusCooldown.getValue(TimeUtil.DAY_IN_MILLIS * 2);
    }




    public static boolean logAdminShopUsage() {
        return logAdminShop.getValue(true);
    }

    public static boolean areTaxesEnabled() {
        return taxesEnabled.getValue(false);
    }

    public static boolean logNormalShopUsage() {
        return logNormalShop.getValue(false);
    }

    public static boolean allowOtherPlayerNameNicks() {
        return allowOtherPlayerNicks.getValue(false);
    }

    public static boolean isEventActive() {
        return crownEventActive.getValue(false);
    }

    public static boolean isEventTimed() {
        return crownEventIsTimed.getValue(false);
    }

    public static boolean shouldHulkSmashPoles()  {
        return hulkSmashPoles.getValue(true);
    }

    public static boolean isEndOpen()  {
        return endOpen.getValue(false);
    }




    public static int getTpTickDelay() {
        return tpTickDelay.getValue(60);
    }

    public static int getTpCooldown() {
        return tpCooldown.getValue(60);
    }

    public static int getTpaExpiryTime() {
        return tpaExpiryTime.getValue(2400);
    }

    public static int getStartRhines() {
        return startRhines.getValue(100);
    }

    public static int getBaronPrice() {
        return baronPrice.getValue(500000);
    }

    public static int getMaxMoneyAmount() {
        return maxMoneyAmount.getValue(50000000);
    }

    public static int getGhSpecialReward() {
        return ghSpecialReward.getValue(25000);
    }

    public static int getGhFinalReward() {
        return ghFinalReward.getValue(50000);
    }

    public static int getTreasureMaxPrize() {
        return maxTreasurePrize.getValue(50000);
    }

    public static int getTreasureMinPrize() {
        return minTreasurePrize.getValue(10000);
    }

    public static int getMaxShopEarnings()  {
        return maxShopEarnings.getValue(500000);
    }

    public static int getMaxSignShopPrice()  {
        return maxSignShopPrice.getValue(1000000);
    }




    public static World getTreasureWorld() {
        return treasureWorld.getValue();
    }

    public static Key onFirstJoinKit() {
        return onFirstJoinKit.getValue();
    }
}
