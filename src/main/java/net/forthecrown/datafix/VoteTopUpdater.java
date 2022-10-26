package net.forthecrown.datafix;

import com.bencodez.votingplugin.VotingPluginMain;
import net.forthecrown.user.UserManager;

public class VoteTopUpdater extends DataUpdater {
    @Override
    protected boolean update() throws Throwable {
        var manager = UserManager.get();
        var voteTop = manager.getVotes();

        manager.getUserLookup()
                .entryStream()
                .forEach(entry -> {
                    var id = entry.getUniqueId();
                    var vUser = VotingPluginMain.getPlugin().getUser(id);

                    vUser.cacheData()
                            .cache();

                    int totalVotes = vUser.getAllTimeTotal();

                    if (totalVotes <= 0) {
                        LOGGER.info("{} had 0 or less votes, skipping map addition", id);
                        return;
                    }

                    voteTop.set(id, totalVotes);
                    LOGGER.info("Added {} to vote top map with {} votes", id, totalVotes);
                });

        voteTop.save();
        return true;
    }
}