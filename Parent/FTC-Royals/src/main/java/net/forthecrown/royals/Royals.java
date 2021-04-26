package net.forthecrown.royals;

import net.forthecrown.royals.commands.CommandRoyal;
import net.forthecrown.royals.dungeons.DungeonEvents;
import net.forthecrown.royals.dungeons.bosses.Bosses;
import net.forthecrown.royals.enchantments.EnchantEvents;
import net.forthecrown.royals.enchantments.RoyalEnchants;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Royals extends JavaPlugin implements Listener {

    public static Royals inst;

    public RoyalEnchants enchantments;
    public Bosses bosses;

    public void onEnable() {
        inst = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        enchantments = new RoyalEnchants(this);
        enchantments.registerEnchantments();

        bosses = new Bosses(this);
        bosses.initBosses();

        getServer().getPluginManager().registerEvents(new EnchantEvents(), this);
        getServer().getPluginManager().registerEvents(new DungeonEvents(this), this);

        new CommandRoyal();
    }

    @Override
    public void onDisable() {
        bosses.killAllBosses();
    }
}