package net.forthecrown.cosmetics.travel;

import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class PinkRocketTravelEffect extends TravelEffect {

    // Simple rocket-like particles when going up, nothing more.
    PinkRocketTravelEffect() {
        super("Pink Rocket", new InventoryPos(1, 1),
                Component.text("idk what to put "),
                Component.text("here yet.")
        );
    }

    private void playerSound(Location loc, Player traveler) {
        loc.getWorld().playSound(loc, Sound.WEATHER_RAIN, SoundCategory.MASTER, 4f, 0.5f);
        // Very high volume to increase range -> only for player that's launching
        traveler.playSound(traveler.getLocation().clone().add(0, 20, 0), Sound.WEATHER_RAIN, SoundCategory.MASTER, 10f, 0.5f);
    }

    @Override
    public void onPoleTeleport(CrownUser user, Location from, Location pole) {}

    @Override
    public void onHulkStart(CrownUser user, Location loc) {
        // Lift off
        playerSound(loc, user.getPlayer());
        loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 16, 0.1, 0, 0.1, 0.04, null, true);
    }

    private static final Particle.DustTransition dustTransition = new Particle.DustTransition(Color.fromRGB(255, 146, 112), Color.fromRGB(255, 192, 211), 3.0F); // Put outside tick
    private static final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 192, 211), 3.0F); // Put outside tick
    @Override
    public void onHulkTickUp(CrownUser user, Location loc) {
        loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc.add(0, -11, 0), 4, 0.2, 1, 0.2, 1, dustTransition, true);
        loc.getWorld().spawnParticle(Particle.CRIMSON_SPORE, loc.add(0, -9, 0), 1024, 0.2, 8, 0.2, 1.5, null, true);
        loc.getWorld().spawnParticle(Particle.REDSTONE, loc.add(0, -8, 0), 4, 0.25, 10, 0.25, 1, dustOptions, true);
    }


    @Override
    public void onHulkLand(CrownUser user, Location landing) {}
    @Override
    public void onHulkTickDown(CrownUser user, Location loc) {}


}