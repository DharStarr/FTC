package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;

public class EmoteHug extends CommandEmote {

    public EmoteHug() {
        super("hug", 20*3, "&c❤ &7You're too nice of a person &c❤");

        setDescription("Hugs a player");
        register();
    }

    @Override
    protected int execute(CrownUser user, CrownUser recipient) {
        if(Cooldown.contains(recipient, "Emote_Hug_Received")){
            user.sendMessage("&e" + recipient.getName() + " &7has already received some love lol");
            return -1;
        }

        //Do the hugging
        TextComponent hugClick = ComponentUtils.convertString(ChatColor.RED + "❤ " + ChatColor.YELLOW + user.getName() + ChatColor.RESET + " hugged you" + ChatColor.YELLOW + " ʕっ•ᴥ•ʔっ" + ChatColor.RED + " ❤")
                .clickEvent(ClickEvent.runCommand("/hug " + user.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("Hug them back ❤")));

        recipient.sendMessage(hugClick);
        user.sendMessage("&c❤ &7You hugged &e" + recipient.getName() + " ʕっ•ᴥ•ʔっ &c❤");

        if(recipient.getPlayer().getGameMode() != GameMode.SPECTATOR){
            Cooldown.add(recipient, "Emote_Hug_Received", 10*20);
            new HugTick(recipient);
        }
        return 0;
    }

    @Override
    protected int executeSelf(CrownUser user) {
        user.sendMessage("It's alright to hug yourself &c❤", "We've all got to love ourselves &c❤");
        user.getWorld().spawnParticle(Particle.HEART, user.getLocation().add(0, 0.5, 0), 3, 0.25, 0.25 ,0.25);
        return -1;
    }

    public class HugTick implements Runnable{
        private int i = 0;
        private final int id;
        private final CrownUser user;

        public HugTick(CrownUser user){
            this.user = user;

            id = Bukkit.getScheduler().scheduleSyncRepeatingTask(FtcCore.getInstance(), this, 0, 2);
        }

        @Override
        public void run() {
            if(i == 10*10){
                Bukkit.getScheduler().cancelTask(id);
                Cooldown.remove(user, "Emote_Hug_Received");
                return;
            }

            user.getPlayer().getWorld().spawnParticle(Particle.HEART, user.getPlayer().getLocation().clone().add(0, 1, 0), 1, 0.25, 0.25 ,0.25);
            i++;
        }
    }
}