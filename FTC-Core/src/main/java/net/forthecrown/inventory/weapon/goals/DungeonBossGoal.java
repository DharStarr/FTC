package net.forthecrown.inventory.weapon.goals;

import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class DungeonBossGoal implements WeaponKillGoal {
    private final Key key;
    private final int rank, goal;
    private final DungeonBoss<?> boss;

    public DungeonBossGoal(DungeonBoss<?> boss, int goal, int rank) {
        this.rank = rank;
        this.goal = goal;
        this.boss = boss;

        this.key = WeaponGoal.createKey(rank, "dboss_" + boss.getName().toLowerCase().replaceAll(" ", "_"));
    }

    @Override
    public int getGoal() {return goal;}

    @Override
    public int getRank() {return rank;}

    @Override
    public @NotNull Key key() {return key;}

    @Override
    public Component loreDisplay() {return Component.text("Kill " + boss.getName());}

    @Override
    public boolean test(Entity entity) {
        return entity.getPersistentDataContainer().has(Bosses.BOSS_TAG, PersistentDataType.BYTE)
                && entity.getType() == boss.getBossEntity().getType();
    }
}