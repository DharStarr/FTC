package net.forthecrown.events.dynamic;

import net.forthecrown.core.Crown;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.FtcUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitTask;

import static org.bukkit.Bukkit.getPluginManager;
import static org.bukkit.Bukkit.getScheduler;

public class RegionVisitListener implements Listener {
    private static final byte TICKS_PER_TICK = 5; //Nice name, I know

    private final FtcUser user;

    public RegionVisitListener(CrownUser user) {
        this.user = (FtcUser) user;
    }

    public void beginListening() {
        user.visitListener = this;

        getPluginManager().registerEvents(this, Crown.inst());
        tickTask = getScheduler().runTaskTimer(Crown.inst(), this::tick, TICKS_PER_TICK, TICKS_PER_TICK);
    }

    private short ticks = 60 * (20 / TICKS_PER_TICK);
    private BukkitTask tickTask;

    private void tick() {
        ticks--;
        if(ticks < 1) {
            unregister();
            return;
        }

        TravelEffect effect = user.getCosmeticData().getActiveTravel();
        if(effect != null) effect.onHulkTick(user.getLocation());
    }

    public void unregister() {
        HandlerList.unregisterAll(this);

        user.visitListener = null;
        if(!tickTask.isCancelled()) tickTask.cancel();

        TravelEffect effect = user.getCosmeticData().getActiveTravel();
        if(effect != null) effect.onHulkLand(user.getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if(!user.getUniqueId().equals(event.getEntity().getUniqueId())) return;
        if(event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        event.setCancelled(true);
        unregister();
    }
}
