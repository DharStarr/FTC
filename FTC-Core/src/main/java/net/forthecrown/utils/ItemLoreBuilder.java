package net.forthecrown.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.apache.commons.lang3.Validate;

import java.util.List;

public class ItemLoreBuilder {
    private final List<Component> lore;

    public ItemLoreBuilder() {
        this(new ObjectArrayList<>());
    }

    public ItemLoreBuilder(List<Component> lore) {
        this.lore = lore;
    }

    public ItemLoreBuilder addAll(Component... lore) {
        for (Component c: Validate.noNullElements(lore)) {
            add(c);
        }

        return this;
    }

    public ItemLoreBuilder addAll(String... lore) {
        for (String s: Validate.noNullElements(lore)) {
            add(s);
        }

        return this;
    }

    public ItemLoreBuilder addAll(Iterable<Component> lore) {
        for (Component c: Validate.notNull(lore)) {
            add(c);
        }

        return this;
    }

    public ItemLoreBuilder addAllStrings(Iterable<String> lore) {
        for (String s: Validate.notNull(lore)) {
            add(s);
        }

        return this;
    }

    public ItemLoreBuilder add(Component c) {
        lore.add(ChatUtils.renderToSimple(c));
        return this;
    }

    public ItemLoreBuilder add(String s) {
        return add(ChatUtils.stringToNonItalic(s, true));
    }

    public ItemLoreBuilder addEmpty() {
        return add(Component.empty());
    }

    public ItemLoreBuilder addAll(Style style, Component... arr) {
        for (Component c: Validate.noNullElements(arr)) {
            add(c.style(style));
        }

        return this;
    }

    public List<Component> getLore() {
        return lore;
    }
}