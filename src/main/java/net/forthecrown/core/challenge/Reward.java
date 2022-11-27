package net.forthecrown.core.challenge;


import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.script.Script;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.format.UnitFormat;
import net.forthecrown.utils.text.writer.TextWriter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.GenericMath;

@Getter
@Builder(builderClassName = "Builder")
@Data
public class Reward {
    public static final String
            KEY_SCALAR = "scalar",
            KEY_BASE = "base",
            KEY_GEMS = "gems",
            KEY_RHINES = "rhines",
            KEY_GUILDEXP = "guildExp",
            KEY_ITEM = "item",
            KEY_SCRIPT = "script";

    public static final RewardField EMPTY_INT = (user, streak) -> 0;

    public static final Reward EMPTY = new Reward(
            EMPTY_INT, EMPTY_INT, EMPTY_INT, null, null
    );

    private final RewardField rhines;
    private final RewardField gems;
    private final RewardField guildExp;

    private final ItemStack item;
    private final String claimScript;

    public ItemStack getItem() {
        return ItemStacks.isEmpty(item) ? null : item.clone();
    }

    public boolean isEmpty() {
        if (this == EMPTY) {
            return true;
        }

        return rhines == EMPTY_INT
                && gems == EMPTY_INT
                && guildExp == EMPTY_INT
                && ItemStacks.isEmpty(item)
                && Strings.isNullOrEmpty(claimScript);
    }

    public boolean isEmpty(User user, int streak) {
        if (isEmpty()) {
            return true;
        }

        int rhines = this.rhines.getValue(user, streak);
        int gems = this.gems.getValue(user, streak);
        int guildExp = this.guildExp.getValue(user, streak);

        return rhines < 1
                && gems < 1
                && guildExp < 1
                && ItemStacks.isEmpty(item)
                && Strings.isNullOrEmpty(claimScript);
    }

    public void give(User user, int streak) {
        int rhineReward = rhines.getValue(user, streak);
        int gemReward = gems.getValue(user, streak);
        int guildReward = guildExp.getValue(user, streak);

        if (rhineReward > 0) {
            user.addBalance(rhineReward);
        }

        if (gemReward > 0) {
            user.addGems(0);
        }

        var guild = user.getGuild();
        if (guildReward > 0 && guild != null) {
            var member = guild.getMember(user.getUniqueId());
            member.addExpEarned(guildReward);
        }

        if (ItemStacks.notEmpty(item)) {
            Util.giveOrDropItem(
                    user.getInventory(),
                    user.getLocation(),
                    item.clone()
            );
        }

        if (!Strings.isNullOrEmpty(claimScript)) {
            Script.read(claimScript)
                    .invoke("onRewardClaim", user, streak);
        }
    }

    public void write(TextWriter writer,
                      @Nullable User viewer,
                      int streak
    ) {
        if (isEmpty() || viewer == null)  {
            return;
        }

        writer.field("Rewards", "");

        int rhines = this.rhines.getValue(viewer, streak);
        int gems = this.gems.getValue(viewer, streak);
        int guildExp = this.guildExp.getValue(viewer, streak);

        if (rhines > 0) {
            writer.field("Rhines", UnitFormat.rhines(rhines));
        }

        if (gems > 0) {
            writer.field("Gems", UnitFormat.gems(gems));
        }

        if (guildExp > 0) {
            writer.field("Guild Exp", Text.NUMBER_FORMAT.format(guildExp));
        }

        if (ItemStacks.notEmpty(item)) {
            writer.field("Item", Text.itemAndAmount(item));
        }

        if (FTC.inDebugMode()
                && !Strings.isNullOrEmpty(claimScript)
        ) {
            writer.field("Script", claimScript);
        }
    }

    /* --------------------------- SERIALIZATION ---------------------------- */

    public static Reward deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return builder()
                .rhines(readReward(json.get(KEY_RHINES)))
                .gems(readReward(json.get(KEY_GEMS)))
                .guildExp(readReward(json.get(KEY_GUILDEXP)))

                .item(json.getItem(KEY_ITEM))
                .claimScript(json.getString(KEY_SCRIPT))

                .build();
    }

    private static RewardField readReward(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return EMPTY_INT;
        }

        if (element.isJsonPrimitive()) {
            return new FixedRewardField(element.getAsInt());
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();

            if (array.size() == 0) {
                throw Util.newException("Reward value array is empty!");
            }

            int[] values = new int[array.size()];

            for (int i = 0; i < array.size(); i++) {
                values[i] = array.get(i).getAsInt();
            }

            return new ArrayRewardField(values);
        }

        var obj = JsonWrapper.wrap(element.getAsJsonObject());
        int base = obj.getInt(KEY_BASE, 1);
        float scalar = obj.getFloat(KEY_SCALAR, 1F);

        return new ScaledRewardField(base, scalar);
    }

    /* ---------------------------- SUB CLASSES ----------------------------- */

    public interface RewardField {
        int getValue(User user, int streak);
    }

    public record FixedRewardField(int value) implements RewardField {
        @Override
        public int getValue(User user, int streak) {
            return value;
        }
    }

    public record ScaledRewardField(int base, float scalar) implements
            RewardField {
        @Override
        public int getValue(User user, int streak) {
            float streakF = Math.max(streak, 1.0F);
            return (int) (base * (streakF * scalar));
        }
    }

    public record ArrayRewardField(int[] values) implements RewardField {
        @Override
        public int getValue(User user, int streak) {
            int index = GenericMath.clamp(streak, 0, values.length - 1);
            return values[index];
        }
    }
}