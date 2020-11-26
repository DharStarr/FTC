package ftc.bigcrown.challenges;

import ftc.bigcrown.Main;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class GenericChallenge implements Listener {
	
	/*
	 * This class is used to store all common code (functions and vars) between all challenges.
	 * Functions like startChallenge are different for each challenge, so we'll define them there and not here.
	 * 
	 * Because of this, only challenge-subclasses need to implement the interface. If a function is not found in their class, 
	 * they look in here. That's why it doesn't want getPlayer() in each subclass for example, it finds it here.
	 * 
	 * There are only set-functions for vars that don't get a value by default or in the constructor, 
	 * so each subclass can set the value on their own in their constructor.
	 * 
	 * Use "extends GenericChallenge" in each subclass and "super(player, ChallengeType);" in their constructor.
	 */

	private Player player;
	private String objectiveName;
	private Location startLocation;
	private Location returnLocation;
	private ChallengeType challengeType;
	private boolean challengeCancelled = false;
	private int startScore;

    public GenericChallenge(@Nonnull Player player, @Nonnull ChallengeType challengeType) {
    	Main.plugin.setChallengeInUse(getChallengeType(), true);
        this.player = player;
        this.challengeType = challengeType;
    }
    
    // Getters & Setters:
    
    public Player getPlayer() {
        return this.player;
    }
    
    public ChallengeType getChallengeType() {
        return this.challengeType;
    }
    
    public int getStartScore() {
    	return this.startScore;
    }
   
    public String getObjectiveName() {
    	return this.objectiveName;
    }
    
    public void setObjectiveName(String name) {
    	this.objectiveName = name;
    }

    public Location getStartLocation() {
        return this.startLocation;
    }
    
    public void setStartLocation(Location loc) {
    	this.startLocation = loc;
    }

    public Location getReturnLocation() {
        return this.returnLocation;
    }
    
    public void setReturnLocation(Location location) {
        this.returnLocation = location;
    }

    public boolean isChallengeCancelled() {
        return this.challengeCancelled;
    }
    
    public void setChallengeCancelled(boolean cancel) {
		this.challengeCancelled = cancel;
	}
    
    
    
    // Other functions all classes have in common:
    
    
    // Returns an Objective object, based on the objective name.
    public Objective getScoreboardObjective() {
        Scoreboard mainScoreboard = Main.plugin.getServer().getScoreboardManager().getMainScoreboard();
        return mainScoreboard.getObjective(getObjectiveName());
    }
    
    // Returns the difference in score between the start & end of a challenge,
    // used to display how much points they earned.
	public int calculateScore() {
		Score score = getScoreboardObjective().getScore(getPlayer().getName());
		return score.getScore() - getStartScore();
	}
	
	// Teleport to where player opened present:
    public void teleportBack() {
    	Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
			getPlayer().teleport(getReturnLocation().add(0, 1, 0));
			getPlayer().playSound(getReturnLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
			Main.plugin.setChallengeInUse(getChallengeType(), false);
		}, 60L);
    }
    
    // Score before challenge, used to show how many points they earned:
	public void setStartScore() {
		Score score = this.getScoreboardObjective().getScore(this.getPlayer().getName());
		this.startScore = score.getScore();
	}
	
	
	// Events

    // This is something that should always happen, no matter what challenge.
    // But in the challenge subclasses, we still need to properly clean challenges, such as kill bats.
    // (Put a delay on it to make sure other events get executed first)
    @EventHandler
    public void onLogoutWhileInChallenge(PlayerQuitEvent event) {
        if (getPlayer() == null) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
        	if (event.getPlayer().getName() == getPlayer().getName()) {
                setChallengeCancelled(true);
                Main.plugin.setChallengeInUse(getChallengeType(), false);
                Main.plugin.playersThatQuitDuringChallenge.add(getPlayer().getName());
            }
        }, 5);
    }
}