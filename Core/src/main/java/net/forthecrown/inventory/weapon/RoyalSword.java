package net.forthecrown.inventory.weapon;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.inventory.RoyalItem;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

/**
 * A RoyalSword is a special in-game sword, this is the class which
 * represents it... shocking, I know.
 */
public class RoyalSword extends RoyalItem {
    private int rank = 1;
    private WeaponUpgrade nextUpgrade;
    private WeaponUpgrade waitingUpdate;
    private Object2IntMap<WeaponGoal> goalsAndProgress = new Object2IntOpenHashMap<>();

    /**
     * Load constructor, loads all needed data from item's NBT
     * @param item The item to load from
     */
    public RoyalSword(ItemStack item) {
        super(item);

        load();
    }

    /**
     * Creation constructor, adds the needed data to the item's NBT.
     * @param owner The owner of the item
     * @param item The item itself.
     */
    public RoyalSword(UUID owner, ItemStack item) {
        super(owner, item);

        setGoals(RoyalWeapons.getGoalsAtRank(1));
    }

    @Override
    protected void readNBT(CompoundTag tag) {
        this.rank = tag.getInt("rank");
        this.nextUpgrade = RoyalWeapons.getUpgrade(rank+1);

        //No need to null this, if it's null,
        //the tag returns an empty compound
        CompoundTag goalsTag = tag.getCompound("goals");

        for (String s: goalsTag.getAllKeys()) {
            Key k = FtcUtils.parseKey(s);
            int progress = goalsTag.getInt(s);

            WeaponGoal goal = Registries.WEAPON_GOALS.get(k);

            //If the goal wasn't found, warn console and ignore it
            if(goal == null) {
                Crown.logger().warning("Found null goal in RoyalSword. Owned by:" + getOwner() + " Goal: " + s);
                continue;
            }

            goalsAndProgress.put(goal, progress);
        }
    }

    @Override
    protected void onUpdate(ItemStack item, ItemMeta meta, CompoundTag tag) {
        tag.putInt("rank", rank);

        //Serialize goals, same as map, goal 2 progress
        CompoundTag goalsTag = new CompoundTag();
        for (Object2IntMap.Entry<WeaponGoal> e: goalsAndProgress.object2IntEntrySet()) {
            goalsTag.putInt(e.getKey().key().asString(), e.getIntValue());
        }

        tag.put("goals", goalsTag);

        //If there's an upgrade waiting to be applied, apply it and then null it.
        if(waitingUpdate == null) return;
        waitingUpdate.apply(item, meta, tag);

        waitingUpdate = null;
    }

    @Override
    protected List<Component> createLore() {
        List<Component> lore = new ObjectArrayList<>();

        //Add the rank identifier
        lore.add(
                Component.text("Rank " + FtcUtils.arabicToRoman(rank))
                        .style(nonItalic(NamedTextColor.GRAY))
        );

        //The bearer of this... bla bla
        addFlavorText(lore);

        //Add goal text, if there are goals to go for
        if(!goalsAndProgress.isEmpty()) {
            //If we've got multiple entries we need to display the lore differently.
            boolean multiEntry = goalsAndProgress.size() > 1;

            //Instead of 1 goal, we have a bullet list if we have several
            if(multiEntry) {
                lore.add(
                        Component.text("Goals:")
                                .style(nonItalic(NamedTextColor.AQUA))
                );
            }

            //For every goal
            for (Object2IntMap.Entry<WeaponGoal> e: goalsAndProgress.object2IntEntrySet()) {
                TextComponent.Builder builder = Component.text()
                        .style(nonItalic(NamedTextColor.AQUA));

                //If multiple goals, add bullet
                if(multiEntry) builder.append(Component.text("• "));

                builder.append(
                        Component.text(e.getIntValue() + "/" + e.getKey().getKillGoal() + " ")
                );

                builder
                        .append(e.getKey().loreDisplay())
                        .append(Component.text(" to rank up!"));

                lore.add(builder.build());
            }
        } else {
            //Else, we've reached max rank
            lore.add(
                    Component.text("Max rank reached!").style(nonItalic(NamedTextColor.YELLOW))
            );
        }

        //If there's something we'll upgrade to once we beat the goal(s),
        //then show the upgrade
        if(nextUpgrade != null) {
            lore.add(
                    Component.text("Next upgrade: ")
                            .style(nonItalic(NamedTextColor.GRAY))
                            .append(nextUpgrade.loreDisplay())
            );
        }

        //If the owner is not a donator, tell them they could donate :)
        if(!getOwnerUser().hasPermission(Permissions.DONATOR_1)) {
            lore.add(
                    Component.text("Donators can upgrade Royal Tools beyond rank 5")
                            .style(nonItalic(NamedTextColor.DARK_GRAY))
            );
        }

        //Could be NIL, because that's used for a generic display version of the sword.
        if(getOwner() != Util.NIL_UUID) {
            lore.add(
                    Component.text("Owner: ")
                            .style(nonItalic(NamedTextColor.DARK_GRAY))
                            .append(getOwnerUser().nickDisplayName())
            );
        }

        return lore;
    }

    private void addFlavorText(List<Component> lore) {
        final Component border = Component.text("------------------------------").style(nonItalic(NamedTextColor.DARK_GRAY));

        lore.add(border);

        lore.add(
                Component.text("The bearer of this weapon has")
                        .style(nonItalic(NamedTextColor.GOLD))
        );
        lore.add(
                Component.text("proven themselves to the Crown...")
                        .style(nonItalic(NamedTextColor.GOLD))
        );

        lore.add(border);
    }

    public void kill(Player killer, Entity killed) {
        //Test all goals to see if we killed any matching entities
        for (Object2IntMap.Entry<WeaponGoal> e: goalsAndProgress.object2IntEntrySet()) {
            if(!e.getKey().isValidTarget(killed)) continue;

            //If we did, increment kill count by the amount stated in ComVar
            int newVal = e.getIntValue() + ComVars.swordGoalGainPerKill();
            goalsAndProgress.put(e.getKey(), newVal);
        }

        //If we should rank up... rank up
        if(shouldRankUp()) {
            incrementGoal();

            doEffects(
                    killer,
                    killed.getWorld(),
                    killer.getLocation()
            );
        }

        //Always update item
        update();
    }

    public void incrementGoal() {
        goalsAndProgress.clear();
        rank++;

        this.waitingUpdate = nextUpgrade;
        nextUpgrade = RoyalWeapons.getUpgrade(rank + 1);

        setGoals(RoyalWeapons.getGoalsAtRank(rank));
    }

    private void doEffects(Player player, World w, Location l) {
        //Play sounds
        w.playSound(l, Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
        w.playSound(l, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);

        //Spawn totem particles
        for (int i = 0; i <= 5; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(
                    Crown.inst(), () -> w.spawnParticle(
                            Particle.TOTEM, l.getX(), l.getY()+2, l.getZ(),
                            30, 0.2d, 0.1d, 0.2d, 0.275d),
                    i*5L
            );
        }

        //Tell 'em they're a good boy
        player.sendMessage(
                Component.text()
                        .append(Crown.prefix())
                        .append(
                                Component.text("Your sword was upgraded to Rank " + FtcUtils.arabicToRoman(rank) + "!")
                                        .color(NamedTextColor.GRAY)
                        )
                        .build()
        );
    }

    /**
     * Tests to see if the sword should be ranked up
     * @return Whether to rank up the sword.
     */
    public boolean shouldRankUp() {
        if(rank == 5 && !getOwnerUser().hasPermission(Permissions.DONATOR_1)) return false;

        for (Object2IntMap.Entry<WeaponGoal> e: goalsAndProgress.object2IntEntrySet()) {
            if(e.getIntValue() < e.getKey().getKillGoal()) return false;
        }

        return true;
    }

    /**
     * Sets the rank
     * @param rank The new rank
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * Gets the sword's rank
     * @return The sword's rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Gets the upgrade the sword will receive when it's rank upgrades
     * @return Next rank's upgrade, null, if none
     */
    public WeaponUpgrade getNextUpgrade() {
        return nextUpgrade;
    }

    /**
     * Sets the next upgrade
     * @param nextUpgrade New next rank's upgrade
     */
    public void setNextUpgrade(WeaponUpgrade nextUpgrade) {
        this.nextUpgrade = nextUpgrade;
    }

    /**
     * Gets a map of goal 2 progress
     * @return Progress map of sword's goals, may be empty if at max level
     */
    public Object2IntMap<WeaponGoal> getGoalsAndProgress() {
        return goalsAndProgress;
    }

    /**
     * Sets the current goals and their progress
     * @param goalsAndProgress The goals and their progress
     */
    public void setGoalsAndProgress(Object2IntMap<WeaponGoal> goalsAndProgress) {
        this.goalsAndProgress = goalsAndProgress;
    }

    /**
     * Sets the goals
     * @param goals The goals to set for the sword
     */
    public void setGoals(List<WeaponGoal> goals) {
        for (WeaponGoal g: goals) {
            this.goalsAndProgress.put(g, 0);
        }
    }
}
