package net.forthecrown.useables.checks;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.InteractionUtils;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class CheckHasitem implements UsageCheck<CheckHasitem.CheckInstance> {
    public static Key KEY = Keys.ftccore("has_item");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new CheckInstance(InteractionUtils.parseGivenItem(source, reader));
    }

    @Override
    public CheckInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new CheckInstance(JsonUtils.readItem(element));
    }

    @Override
    public JsonElement serialize(CheckInstance value) {
        return JsonUtils.writeItem(value.getItem());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return InteractionUtils.listItems(context, builder);
    }

    public static class CheckInstance implements UsageCheckInstance {
        private final ItemStack item;

        CheckInstance(ItemStack item) {
            this.item = item;
        }

        public ItemStack getItem() {
            return item.clone();
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "item=" + item + '}';
        }

        @Override
        public Component failMessage(Player player) {
            return Component.text("You don't have the required item");
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }

        @Override
        public boolean test(Player player) {
            return player.getInventory().containsAtLeast(getItem(), getItem().getAmount());
        }
    }
}