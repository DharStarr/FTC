package net.forthecrown.core.challenge;


import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.inventory.ItemStack;

@Getter
@Builder(builderClassName = "Builder")
@RequiredArgsConstructor
public class JsonReward {
    public static final JsonReward EMPTY = new JsonReward(0, 0, 0, null);

    private final int rhines;
    private final int gems;
    private final int guildExp;
    private final ItemStack item;

    public ItemStack getItem() {
        return ItemStacks.isEmpty(item) ? null : item.clone();
    }

    public boolean isEmpty() {
        return rhines < 1
                && gems < 1
                && guildExp < 1
                && ItemStacks.isEmpty(item);
    }

    public void give(User user) {
        if (gems > 0) {
            user.addGems(gems);
        }

        if (rhines > 0) {
            user.addBalance(rhines);
        }

        if (guildExp > 0) {
            var guild = user.getGuild();

            if (guild != null) {
                var member = guild.getMember(user.getUniqueId());
                member.addExpEarned(guildExp);
            }
        }

        if (!ItemStacks.isEmpty(item)) {
            Util.giveOrDropItem(
                    user.getInventory(),
                    user.getLocation(),
                    getItem()
            );
        }
    }
}