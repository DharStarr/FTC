package net.forthecrown.core;

import net.forthecrown.commands.manager.Commands;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.challenge.ChallengeLogs;
import net.forthecrown.core.challenge.ChallengeManager;
import net.forthecrown.core.config.ConfigManager;
import net.forthecrown.core.config.Configs;
import net.forthecrown.core.holidays.ServerHolidays;
import net.forthecrown.core.resource.ResourceWorldTracker;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.datafix.Transformers;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.enchantments.FtcEnchants;
import net.forthecrown.dungeons.level.LevelManager;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.Transactions;
import net.forthecrown.events.Events;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.unlockables.Unlockables;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.log.DataManager;
import net.forthecrown.structure.Structures;
import net.forthecrown.useables.Usables;
import net.forthecrown.user.Components;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.text.ChatEmotes;
import net.forthecrown.waypoint.WaypointManager;
import net.forthecrown.waypoint.WaypointProperties;
import net.forthecrown.waypoint.type.WaypointTypes;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/**
 * A class which loads and creates everything the FTC plugin does and needs.
 * Aka a class which starts the plugin
 */
final class BootStrap {
    private BootStrap() {}

    private static final Logger LOGGER = FTC.getLogger();
    static final String INIT_METHOD = "init";

    static void init() {
        RoyalCommandException.ENABLE_HOVER_STACK_TRACE = FTC.inDebugMode();

        init(Transformers.class);
        init(Unlockables.class);
        init(GuildManager.class);
        init(UserManager.class);
        init(ChatEmotes.class);
        init(ServerHolidays.class);
        init(Structures.class);
        init(WaypointTypes.class);
        init(WaypointManager.class);
        init(WaypointProperties.class);
        init(FtcEnchants.class);
        init(Bosses.class);
        init(ExtendedItems.class);
        init(Cosmetics.class);
        init(Components.class);
        init(Properties.class);
        init(Usables.class);
        init(ResourceWorldTracker.class);
        init(Commands.class);
        init(Events.class);
        init(LevelManager.class);
        init(ChallengeLogs.class);
        init(Transactions.class);
        init(DataManager.class);
        init(ChallengeManager.class);
        init(Announcer.class);
        init(Economy.class);
        init(Configs.class);

        FTC.getPlugin().saveResource("banned_words.json", true);
        BannedWords.load();

        DayChange.get().schedule();
        AutoSave.get().schedule();

        ServerIcons.loadIcons();
        Transformers.runCurrent();

        Punishments.get().reload();
        Economy.get().reload();

        ConfigManager.get().load();
    }

    static void init(Class<?> c) {
        try {
            Method init = c.getDeclaredMethod(INIT_METHOD);
            init.setAccessible(true);

            Validate.isTrue(Modifier.isStatic(init.getModifiers()), "% method is not static", INIT_METHOD);
            Validate.isTrue(init.getReturnType() == Void.TYPE, "%s method return value is not void", INIT_METHOD);

            init.invoke(null);

            LOGGER.debug("{} Initialized", c.getSimpleName());
        } catch (Throwable t) {
            LOGGER.error("Couldn't initialize {}:", c.getSimpleName(), t);
        }
    }
}