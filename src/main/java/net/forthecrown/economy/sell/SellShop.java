package net.forthecrown.economy.sell;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.utils.inventory.menu.Menu;
import net.forthecrown.utils.inventory.menu.Menus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class SellShop {
    /** Registry of sell shop menus */
    @Getter
    private final Registry<SellShopMenu> menus = Registries.newRegistry();

    /**
     * Directory the sellshops are in, this directory
     * must include the <code>shops.json</code> file
     * from which the shop data is read
     */
    private final Path directory;

    /** Main sellshop menu */
    @Getter
    private Menu mainMenu;

    /** Global item price map */
    @Getter
    private final ItemPriceMap priceMap = new ItemPriceMap();

    public void load() {
        SerializationHelper.readJsonFile(getPath(), this::load);
    }

    private void load(JsonWrapper json) {
        this.menus.clear();
        this.priceMap.clear();

        var builder = Menus.builder(Menus.sizeFromRows(4), "FTC Shop")
                .addBorder()
                .add(4, 1, SellShopNodes.WEBSTORE);

        for (var e: json.entrySet()) {
            var name = e.getKey();
            var element = e.getValue();
            var menuJson = JsonWrapper.wrap(element.getAsJsonObject());

            var reader = new MenuReader(directory, menuJson);
            var menu = reader.read(this);

            priceMap.addAll(menu.getPriceMap());
            builder.add(reader.getSlot(), Menus.createOpenNode(menu.getInventory(), menu.getButton()));

            menus.register(name, menu);
        }

        this.mainMenu = builder.build();
    }

    public Path getPath() {
        return directory.resolve("shops.json");
    }

    public void createDefaults() {
        final String[] defaults = {
                "shops.json",
                "crops.shop",
                "minerals.shop",
                "mining.shop",
                "drops.shop",
        };

        for (var s: defaults) {
            var input = FTC.getPlugin().getResource("sellshops/" + s);
            Path path = directory.resolve(s);

            try {
                if (!Files.exists(directory)) {
                    Files.createDirectories(directory);
                }

                var output = Files.newOutputStream(path);
                output.write(input.readAllBytes());
            } catch (IOException e) {
                FTC.getLogger().error("Couldn't save sellshop file defaults", e);
            }
        }

    }
}