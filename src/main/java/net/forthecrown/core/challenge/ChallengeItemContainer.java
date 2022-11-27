package net.forthecrown.core.challenge;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.TagUtil;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.List;
import java.util.Random;

@Getter
@RequiredArgsConstructor
public class ChallengeItemContainer {
    public static final String
            TAG_ACTIVE = "active",
            TAG_POTENTIALS = "potential",
            TAG_PREVIOUS = "previous";

    private final String challengeKey;

    @Setter
    private ItemStack active = null;

    private final List<ItemStack> used = new ObjectArrayList<>();
    private final List<ItemStack> potentials = new ObjectArrayList<>();

    /* ------------------------------ METHODS ------------------------------- */

    public boolean isEmpty() {
        return potentials.isEmpty() && !hasActive();
    }

    public boolean hasActive() {
        return ItemStacks.notEmpty(active);
    }

    public void fillFrom(Inventory inventory) {
        var it = ItemStacks.nonEmptyIterator(inventory);

        while (it.hasNext()) {
            var next = it.next();
            var meta = next.getItemMeta();

            if (meta instanceof BlockStateMeta state
                    && state.getBlockState() instanceof InventoryHolder holder
            ) {
                fillFrom(holder.getInventory());
                continue;
            }

            potentials.add(next.clone());
        }
    }

    public ItemStack next(Random random) {
        if (potentials.isEmpty()) {
            return null;
        }

        if (potentials.size() == 1) {
            return potentials.get(0).clone();
        }

        ItemStack stack = null;
        short safeGuard = 512;

        while (stack == null || used.contains(stack)) {
            stack = potentials.get(random.nextInt(potentials.size()));

            if (--safeGuard < 0) {
                break;
            }
        }

        return stack == null ? null : stack.clone();
    }

    public void clear() {
        active = null;
        used.clear();
        potentials.clear();
    }

    /* --------------------------- SERIALIZATION ---------------------------- */

    public void save(CompoundTag tag) {
        if (hasActive()) {
            tag.put(TAG_ACTIVE, ItemStacks.save(active));
        }

        if (!used.isEmpty()) {
            tag.put(TAG_PREVIOUS,
                    TagUtil.writeCollection(used, ItemStacks::save)
            );
        }

        if (!potentials.isEmpty()) {
            tag.put(TAG_POTENTIALS,
                    TagUtil.writeCollection(potentials, ItemStacks::save)
            );
        }
    }

    public void load(CompoundTag tag) {
        clear();

        if (tag.contains(TAG_ACTIVE)) {
            active = ItemStacks.load(tag.getCompound(TAG_ACTIVE));
        }

        if (tag.contains(TAG_PREVIOUS)) {
            used.addAll(
                    TagUtil.readCollection(
                            tag.get(TAG_PREVIOUS),
                            tag1 -> ItemStacks.load((CompoundTag) tag1)
                    )
            );
        }

        if (tag.contains(TAG_POTENTIALS)) {
            potentials.addAll(
                    TagUtil.readCollection(
                            tag.get(TAG_POTENTIALS),
                            tag1 -> ItemStacks.load((CompoundTag) tag1)
                    )
            );
        }
    }
}