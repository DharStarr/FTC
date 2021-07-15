package net.forthecrown.valhalla.data;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.utils.math.BlockPos;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

public class ChestGroup implements Keyed {

    private final Key groupKey;

    private Key lootTableKey;
    private byte maxChests;
    private ObjectList<BlockPos> possibleLocations;

    public ChestGroup(Key groupKey) {
        this.groupKey = groupKey;
    }

    public ChestGroup(Key groupKey, Key lootTableKey, byte maxChests, ObjectList<BlockPos> possibleLocations) {
        this.groupKey = groupKey;
        this.lootTableKey = lootTableKey;
        this.maxChests = maxChests;
        this.possibleLocations = possibleLocations;
    }

    public byte getMaxChests() {
        return maxChests;
    }

    public void setMaxChests(byte maxChests) {
        this.maxChests = maxChests;
    }

    public Key getLootTableKey() {
        return lootTableKey;
    }

    public void setLootTableKey(Key lootTableKey) {
        this.lootTableKey = lootTableKey;
    }

    public ObjectList<BlockPos> getPossibleLocations() {
        return possibleLocations;
    }

    public void setPossibleLocations(ObjectList<BlockPos> possibleLocations) {
        this.possibleLocations = possibleLocations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ChestGroup group = (ChestGroup) o;

        return new EqualsBuilder()
                .append(getMaxChests(), group.getMaxChests())
                .append(key(), group.key())
                .append(getLootTableKey(), group.getLootTableKey())
                .append(getPossibleLocations(), group.getPossibleLocations())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(key())
                .append(getLootTableKey())
                .append(getMaxChests())
                .append(getPossibleLocations())
                .toHashCode();
    }

    @Override
    public @NotNull Key key() {
        return groupKey;
    }
}
