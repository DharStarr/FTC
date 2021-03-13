package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.api.CrownUser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class EmotePoke extends CommandEmote {

    public EmotePoke(){
        super("poke", 5*20, "You poke people too often lol");

        setPermission("ftc.emotes");
        setDescription("Pokes another player.");
        register();
    }

    private final static List<String> pokeOwies = Arrays.asList("stomach", "back", "arm", "butt", "cheek", "neck");

    @Override
    protected int execute(CrownUser sender, CrownUser recipient) {
        int pokeOwieInt = (int)(Math.random()*pokeOwies.size()); //The random int that determines what body part they'll poke lol
        sender.sendMessage("You poked " + ChatColor.YELLOW + recipient.getName() + "'s " + ChatColor.RESET + pokeOwies.get(pokeOwieInt));

        if(recipient.getPlayer().getGameMode() != GameMode.SPECTATOR){
            Player target = recipient.getPlayer();
            Location targetLoc = recipient.getPlayer().getLocation();

            recipient.sendMessage(ChatColor.YELLOW + sender.getName() + ChatColor.RESET + " poked your " + pokeOwies.get(pokeOwieInt));
            targetLoc.getWorld().playSound(target.getLocation(), Sound.ENCHANT_THORNS_HIT, 3.0F, 1.8F);
            target.setVelocity(target.getVelocity().add(target.getLocation().getDirection().normalize().multiply(-0.3).setY(.1)));
        }
        return 0;
    }

    @Override
    protected int executeSelf(CrownUser user) {
        Player player = user.getPlayer();
        player.sendMessage("You poked yourself! Weirdo"); //Damn, some people really be weird, pokin themselves, couldn't be me ( ._.)
        player.getWorld().playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, 3.0F, 1.8F);
        return 0;
    }
}
