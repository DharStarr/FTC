package ftc.bigcrown.challenges;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import ftc.bigcrown.Main;
import net.md_5.bungee.api.ChatColor;

public class KillEndermenChallenge extends GenericChallenge implements Challenge, Listener {
	
	private TimerCountingDown timer;
	private Location startLocation = new Location(Bukkit.getWorld("world"), -4.5, 5, 37.5); // TODO
	
	public KillEndermenChallenge(Player player) {
		super(player, ChallengeType.ENDERMEN);
		if (player == null || Main.plugin.getChallengeInUse(getChallengeType())) return;

		// All needed setters from super class:
 		setObjectiveName("endermenKilled");
 		setReturnLocation(getPlayer().getLocation());
 		setStartLocation(this.startLocation);
 		setStartScore();

		this.startChallenge();
	}

	public void startChallenge() {
		// Teleport player to challenge:
		this.getPlayer().teleport(getStartLocation());
		this.getPlayer().playSound(getStartLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

		// Send instruction on what to do:
		this.sendTitle();

		// Countdown, so start timer immediately after title:
		KillEndermenChallenge kec = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        @Override
	        public void run() {
	        	if (!isChallengeCancelled()) timer = new TimerCountingDown(kec, 60, true);
	        }
	    }, 50L);
	}

	public void endChallenge() {
		// Timer stopped:
		this.timer = null;
		getPlayer().playSound(this.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2f, 1.5f);

		// Amount of endermen killed:
		int score = calculateScore();
		if (score != 1) this.getPlayer().sendMessage(ChatColor.YELLOW + "You've killed " + score + " endermen!");
		else this.getPlayer().sendMessage(ChatColor.YELLOW + "You've killed 1 enderman!");
		// Add to crown scoreboard:
    	Scoreboard mainScoreboard = Main.plugin.getServer().getScoreboardManager().getMainScoreboard();
    	Score crownScore = mainScoreboard.getObjective("crown").getScore(getPlayer().getName());
    	crownScore.setScore(crownScore.getScore() + score);

		teleportBack();
	}

	public void sendTitle() {
		getPlayer().sendTitle(ChatColor.YELLOW + "Kill Endermen!", ChatColor.GOLD + "January February May", 5, 60, 5);
	}

	@EventHandler
	public void onLogoutWhileInChallenge(PlayerQuitEvent event) {
		if (getPlayer() == null) return;
		if (event.getPlayer().getName() == this.getPlayer().getName()) {
			if (this.timer != null) {
				this.timer.stopTimer(true);
				this.timer = null;
			}
		    Main.plugin.playersThatQuitDuringChallenge.add(this.getPlayer().getName());
		}

	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity().getKiller() == null || this.getPlayer() == null) return;
		if (event.getEntity().getType() != EntityType.ENDERMAN || event.getEntity().getKiller().getName() != this.getPlayer().getName()) return;

		event.getEntity().setCustomNameVisible(true);
		event.getEntity().setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "+1");
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (getPlayer() == null) return;
		if (event.getEntity().getName() == getPlayer().getName()) {
			if (this.timer != null) {
				this.timer.stopTimer(true);
				this.timer = null;
			}
			setChallengeCancelled(true);
			Main.plugin.setChallengeInUse(getChallengeType(), false);
			getPlayer().sendMessage(ChatColor.GRAY + "Challenge failed! No points earned.");
		}
	}

	

}
