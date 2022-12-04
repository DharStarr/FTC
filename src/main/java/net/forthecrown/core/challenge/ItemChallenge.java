package net.forthecrown.core.challenge;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.apache.commons.lang3.Validate;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class ItemChallenge implements Challenge {
    @Setter
    private ItemStack targetItem;

    @Getter
    private final Slot menuSlot;

    @Getter
    private final Reward reward;

    @Getter
    private final ImmutableList<Component> description;

    @Getter
    private final ResetInterval resetInterval;

    /* ------------------------------ METHODS ------------------------------- */

    @Override
    public Component getName() {
        return getTargetItem()
                .map(stack -> {
                    var builder = Component.text()
                            .content("Find ")
                            .color(NamedTextColor.GOLD);

                    if (stack.getAmount() < 2) {
                        builder
                                .append(Component.text("a "))
                                .append(Text.itemDisplayName(stack)
                                                .color(NamedTextColor.YELLOW)
                                );
                    } else {
                        builder.append(Text.itemAndAmount(stack)
                                         .color(NamedTextColor.YELLOW)
                        );
                    }

                    return builder.build();
                })
                .orElseGet(() -> Component.text("Get item"));
    }

    public Optional<ItemStack> getTargetItem() {
        return ItemStacks.isEmpty(targetItem)
                ? Optional.empty()
                : Optional.of(targetItem.clone());
    }

    @Override
    public StreakBasedValue getGoal() {
        return getTargetItem()
                .map(stack -> StreakBasedValue.fixed(stack.getAmount()))
                .orElse(StreakBasedValue.ONE);
    }

    @Override
    public float getGoal(User user) {
        return getTargetItem()
                .map(ItemStack::getAmount)
                .orElse(1);
    }

    @Override
    public StreakCategory getStreakCategory() {
        return StreakCategory.ITEMS;
    }

    @Override
    public void deactivate() {
        targetItem = null;
    }

    @Override
    public String activate(boolean resetting) {
        var manager = ChallengeManager.getInstance();
        var storage = manager.getStorage();

        var holder = ChallengeManager.getInstance()
                .getChallengeRegistry()
                .getHolderByValue(this)
                .orElseThrow();

        String result = null;
        var container = storage.loadContainer(holder);

        if (resetting) {
            var random = container.next(Util.RANDOM);

            if (ItemStacks.notEmpty(random)) {
                container.getUsed().add(random);
                container.setActive(random);
                setTargetItem(random);

                result = ItemStacks.toNbtString(random);
            } else {
                container.setActive(null);
                setTargetItem(null);
            }

            storage.saveContainer(container);
        } else {
            setTargetItem(container.getActive());
        }

        return result;
    }

    @Override
    public void onComplete(User user) {
        Challenges.trigger("daily/shop_challenge", user);
        Challenge.super.onComplete(user);
    }

    @Override
    public void trigger(Object input) {
        var player = ChallengeHandle.getPlayer(input);
        var item = getTargetItem()
                .orElseThrow();

        var inventory = player.getInventory();

        Validate.isTrue(inventory.containsAtLeast(item.clone(), item.getAmount()));
        inventory.removeItemAnySlot(item.clone());

        Challenges.apply(this, holder -> {
            ChallengeManager.getInstance()
                    .getOrCreateEntry(player.getUniqueId())
                    .addProgress(holder, item.getAmount());
        });
    }

    public MenuNode toInvOption() {
        return MenuNode.builder()
                .setItem((user, context) -> {
                    var baseItem = getTargetItem().orElse(null);

                    if (baseItem == null) {
                        return null;
                    }

                    var builder = ItemStacks.toBuilder(baseItem)
                            .setName(getName())
                            .clearLore()
                            .addFlags(ItemFlag.HIDE_ENCHANTS);

                    if (Challenges.hasCompleted(this, user.getUniqueId())) {
                        builder.addEnchant(Enchantment.BINDING_CURSE, 1)
                                .addLore("&aAlready completed!");
                    } else {
                        builder.addLore("&6Click to take from your inventory!");
                    }

                    builder.addLore(
                            "&7Get all the items to complete the challenge."
                    );

                    if (!getDescription().isEmpty()) {
                        builder.addLore("");

                        for (var c: getDescription()) {
                            builder.addLoreRaw(
                                    Text.wrapForItems(c)
                                            .color(NamedTextColor.DARK_GRAY)
                            );
                        }
                    }

                    int streak = Challenges.queryStreak(this, user)
                            .orElse(0);

                    if (!getReward().isEmpty(streak)) {
                        var writer = TextWriters.loreWriter();
                        writer.newLine();
                        writer.newLine();

                        writer.setFieldStyle(
                                Style.style(NamedTextColor.GRAY)
                        );
                        writer.setFieldValueStyle(
                                Style.style(NamedTextColor.GRAY)
                        );

                        getReward().write(writer, streak);
                        builder.addLore(writer.getLore());
                    }

                    return builder.build();
                })

                .setRunnable((user, context, click) -> {
                    click.setCooldownTime(5);

                    if (ItemStacks.isEmpty(targetItem)) {
                        return;
                    }

                    if (Challenges.hasCompleted(this, user.getUniqueId())) {
                        throw Exceptions.format("Challenge already completed");
                    }

                    var inventory = user.getInventory();

                    if (!inventory.containsAtLeast(
                            targetItem,
                            targetItem.getAmount())
                    ) {
                        throw Exceptions.dontHaveItemForShop(targetItem);
                    }

                    trigger(user);
                    click.shouldReloadMenu(true);
                })

                .build();
    }

    /* -------------------------- OBJECT OVERRIDES -------------------------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ItemChallenge challenge)) {
            return false;
        }

        return getResetInterval() == challenge.getResetInterval()
                && Objects.equals(menuSlot, challenge.menuSlot)
                && Objects.equals(description, challenge.description)
                && Objects.equals(reward, challenge.reward);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getResetInterval(),
                menuSlot,
                description,
                reward
        );
    }
}