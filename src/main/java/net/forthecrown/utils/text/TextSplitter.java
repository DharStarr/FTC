package net.forthecrown.utils.text;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.translation.GlobalTranslator;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.Component.text;

@Getter
@RequiredArgsConstructor
class TextSplitter {
    private final Component input;
    private final Pattern pattern;

    private final List<Component> result = new ObjectArrayList<>();

    TextSplitter build() {
        var finalBuilder = split(
                GlobalTranslator.render(input, Locale.ENGLISH),
                text().style(input.style()),
                Style.empty()
        );

        var text = finalBuilder.build();
        if (!Text.plain(text).isEmpty()) {
            result.add(text);
        }

        return this;
    }

    TextComponent.Builder split(Component c, TextComponent.Builder builder, Style parentStyle) {
        var style = c.style()
                .merge(parentStyle, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);

        builder.style(style);

        if (c instanceof TextComponent text) {
            splitText(text, style, builder);
        } else {
            builder.append(builder);
        }

        for (var child: c.children()) {
            var childBuilder = split(child, text().style(style), style);
            var text = childBuilder.build();

            if (!Text.plain(text).isEmpty()) {
                builder.append(text);
            }
        }
        return builder;
    }

    TextComponent.Builder splitText(TextComponent text, Style style, TextComponent.Builder builder) {
        String content = text.content();
        String[] split = pattern.split(content);

        if (split.length == 1) {
            if (Strings.isNullOrEmpty(split[0])) {
                return builder;
            }

            if (content.equals(split[0])) {
                builder.append(text);
                return builder;
            }

            builder = newSplit(builder);
            builder.append(text);
            return builder;
        }

        builder = newSplit(builder)
                .style(style);

        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            result.add(text(s, style));
        }

        return builder;
    }

    private TextComponent.Builder newSplit(TextComponent.Builder builder) {
        var text = builder.build();

        if (!Text.plain(text).isEmpty()) {
            result.add(text);
        }

        return text();
    }
}