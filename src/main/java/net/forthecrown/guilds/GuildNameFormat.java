package net.forthecrown.guilds;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

@RequiredArgsConstructor
public class GuildNameFormat {

    // todo serialize/deserialize
    public static final String
            BRACKETS_KEY = "brackets",
            COLORS_KEY = "colors",
            STYLE_KEY = "style";

    // ==? @RequiredArgsConstructor
    GuildNameFormat(Bracket bracket, Color color, Stylee style) {
        this.bracket = bracket;
        this.color = color;
        this.style = style;
    }

    @Getter @Setter
    private Bracket bracket;
    private Color color;
    private Stylee style;

    @Getter
    protected static final GuildNameFormat DEFAULT = new GuildNameFormat(Bracket.DEFAULT, Color.DEFAULT, Stylee.DEFAULT);


    // Applies the format to the given guild name
    public Component apply(String guildName) {
        // todo: add colors somehow?
        return Component.text(bracket.getOpening(), style.getBracketStyle())
                .append(Component.text(guildName, style.getNameStyle()))
                .append(Component.text(bracket.getClosing(), style.getBracketStyle()));
    }

    @RequiredArgsConstructor
    @Getter
    public enum Bracket {
        DEFAULT("default", "[", "]"),
        ROUND("round", "(", ")"),
        ANGLE("angle", "<", ">"),
        SQUARE_SPECIAL1("specialSquare", "|[", "]|"),
        SQUARE_SPECIAL2("specialSquare", "=[", "]="),
        ;

        @Getter
        private final String key, opening, closing;

        public Component getOpeningBracket(TextColor color) {
            return getOpeningBracket(Style.style(color));
        }

        public Component getOpeningBracket(Style style) {
            return Component.text(opening, style);
        }

        public Component getClosingBracket(TextColor color) {
            return getClosingBracket(Style.style(color));
        }

        public Component getClosingBracket(Style style) {
            return Component.text(opening, style);
        }

        // Default color, default style, preview brackets
        public Component getPreview(String guildName, TextColor primary, TextColor secondary) {
            return getOpeningBracket(secondary)
                    .append(Component.text(guildName, primary))
                    .append(getClosingBracket(secondary));
        }
    }


    interface idkAname {
        // String[] name = [openingBrackets, guildName, closingBrackets]
        // todo: How to do better? Applying colors is weird, I'm sure you have a better way of doing that already
        Component applyColor(String[] name, TextColor primary, TextColor secondary);
    }

    @RequiredArgsConstructor
    @Getter
    public enum Color {
        DEFAULT("default", (name, primary, secondary) -> {
            return Component.text(name[0], secondary)
                    .append(Component.text(name[1], primary))
                    .append(Component.text(name[2], secondary));
        }),
        ALTERNATE("alternate", (name, primary, secondary) -> {
            TextComponent.Builder b = Component.text();
            TextColor colorToUse = primary;

            char[] fullName = String.join("", name).toCharArray();
            for (char c : fullName) {
                b.append(Component.text(c, colorToUse));
                colorToUse = colorToUse == primary ? secondary : primary;
            }

            return b.build();
        }),
        GRADIENT_2COLORS("gradient2", (name, primary, secondary) -> {
            String text = String.join("", name);
            // todo: return gradient boi from "text"
            return Component.text("null");
        }),
        GRADIENT_3COLORS("gradient3", (name, primary, secondary) -> {
            String text = String.join("", name);
            // todo: return gradient boi from "text"
            return Component.text("null");
        }),
        GRADIENT_4COLORS("gradient4", (name, primary, secondary) -> {
            String text = String.join("", name);
            // todo: return gradient boi from "text"
            return Component.text("null");
        }),
        ;

        @Getter
        private final String key;
        private final idkAname applier;

        // Default brackets, default style, preview color
        public Component getPreview(String guildName, TextColor primary, TextColor secondary) {
            return applier.applyColor(
                    new String[]{Bracket.DEFAULT.getOpening(), guildName, Bracket.DEFAULT.getClosing()},
                    primary,
                    secondary
            );
        }
    }


    private static final Style noStyle = Style.style().build();

    @RequiredArgsConstructor
    @Getter
    public enum Stylee {
        DEFAULT("default",
                noStyle,
                noStyle),
        FATB("boldBrackets",
                Style.style(TextDecoration.BOLD),
                noStyle),
        ITALIC("italic",
                Style.style(TextDecoration.ITALIC),
                Style.style(TextDecoration.ITALIC)),
        ITALIC_FATB("boldBracketsItalic",
                Style.style(TextDecoration.ITALIC, TextDecoration.BOLD),
                Style.style(TextDecoration.ITALIC)),
        FAT_STRIKED_B("strikedBoldBrackets",
                Style.style(TextDecoration.STRIKETHROUGH, TextDecoration.BOLD),
                noStyle),
        ;
        @Getter
        private final String key;
        private final Style bracketStyle, nameStyle;

        // Default brackets, default color, preview styles
        public Component getPreview(String guildName, TextColor primary, TextColor secondary) {
            return Component.text(Bracket.DEFAULT.getOpening(), bracketStyle.color(secondary))
                    .append(Component.text(guildName, nameStyle.color(primary)))
                    .append(Component.text( Bracket.DEFAULT.getClosing(), bracketStyle.color(secondary)));
        }
    }

}
