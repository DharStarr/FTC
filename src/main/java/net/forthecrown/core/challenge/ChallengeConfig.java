package net.forthecrown.core.challenge;

import lombok.experimental.UtilityClass;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.config.ConfigData;
import org.bukkit.Location;

@ConfigData(filePath = "challenges.json")
public @UtilityClass class ChallengeConfig {
    public int
            maxDailyChallenges          = 5,
            maxWeeklyChallenges         = 10,
            maxStreak                   = 366;

    public boolean
            allowRepeatingChallenges    = false;

    private Location
            highestStreakLocation       = new Location(Worlds.overworld(), 207.5, 73.15, 188.5),
            streakLeaderboard           = new Location(Worlds.overworld(), 210.5, 72.65, 195.5);

    public Location getHighestStreakLocation() {
        return highestStreakLocation == null
                ? null
                : highestStreakLocation.clone();
    }

    public Location getStreakLeaderboard() {
        return streakLeaderboard == null
                ? null
                : streakLeaderboard.clone();
    }
}