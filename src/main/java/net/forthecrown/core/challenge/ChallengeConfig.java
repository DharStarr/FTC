package net.forthecrown.core.challenge;

import lombok.experimental.UtilityClass;
import net.forthecrown.core.config.ConfigData;

@ConfigData(filePath = "challenges.json")
public @UtilityClass class ChallengeConfig {
    public int
            maxDailyChallenges          = 12,
            maxWeeklyChallenges         = 4;

    public boolean
            allowRepeatingChallenges    = false;
}