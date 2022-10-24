package net.forthecrown.economy;

import lombok.Getter;
import net.forthecrown.core.AutoSave;
import net.forthecrown.core.DayChange;
import net.forthecrown.core.config.ConfigManager;
import net.forthecrown.economy.market.MarketConfig;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.sell.SellShop;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.utils.io.PathUtil;

import java.nio.file.Files;
import java.nio.file.Path;

public class Economy {
    private static final Economy INSTANCE = new Economy();

    @Getter
    private final SellShop sellShop;

    @Getter
    private final ShopManager shops;

    @Getter
    private final MarketManager markets;

    @Getter
    private final Path directory;

    private Economy() {
        this.directory = PathUtil.getPluginDirectory("economy");

        this.sellShop = new SellShop(directory);
        this.markets = new MarketManager(directory);

        this.shops = new ShopManager();

        ConfigManager.get()
                .registerConfig(MarketConfig.class);
    }

    public static Economy get() {
        return INSTANCE;
    }

    static void init() {
        if (!Files.exists(get().getSellShop().getPath())) {
            get().getSellShop().createDefaults();
        }

        DayChange.get()
                .addListener(get().getMarkets());

        AutoSave.get()
                .addCallback(get()::save);
    }

    public void save() {
        shops.save();
        markets.save();
    }

    public void reload() {
        shops.reload();
        markets.load();
        sellShop.load();
    }
}