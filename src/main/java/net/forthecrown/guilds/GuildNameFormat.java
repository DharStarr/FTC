package net.forthecrown.guilds;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.function.Supplier;

import static net.kyori.adventure.text.Component.text;

@Getter
@Setter
@AllArgsConstructor
public class GuildNameFormat {

    // todo serialize/deserialize
    public static final String
            BRACKETS_KEY = "brackets",
            COLORS_KEY = "colors",
            STYLE_KEY = "style";

    public static final int
            OPENING_BRACKET = 0,
            GUILD_NAME = 1,
            CLOSING_BRACKET = 2;

    // Jules:
    // Hey, so I changed this file a bit, first of all, I inadvertently made
    // Color the class that basically compiles the bracket, style and everything
    // else into a single display name, you can get that display name using
    // apply(Guild);
    //
    // I also removed the functional interface you made for the Color enum, and
    // used an abstract method in its place which accepts more parameters, see
    // below for java doc.
    //
    // I'm not confident in the gradients working as I haven't tested them, same
    // with all the colors
    //
    // Otherwise though, thank you a lot, this is good :D
    //   - Jules <3

    private Bracket bracket;
    @Getter @Setter
    private Color color;
    @Getter @Setter
    private Stylee style;

    public static GuildNameFormat createDefault() {
        return new GuildNameFormat(
                Bracket.DEFAULT,
                Color.DEFAULT,
                Stylee.DEFAULT
        );
    }

    // Applies the format to the given guild name
    public Component apply(String guildName) {
        // todo: add colors somehow?
        return text(bracket.getOpening(), style.getBracketStyle())
                .append(text(guildName, style.getNameStyle()))
                .append(text(bracket.getClosing(), style.getBracketStyle()));
    }

    public Component apply(Guild guild) {
        String name = guild.getName();
        TextColor primary = guild.getSettings()
                .getPrimaryColor()
                .getTextColor();

        TextColor secondary = guild.getSettings()
                .getSecondaryColor()
                .getTextColor();

        String[] completeName = {
                bracket.getOpening(),
                name,
                bracket.getClosing()
        };

        return color.apply(
                completeName,
                primary,
                secondary,
                style.getBracketStyle(),
                style.getNameStyle()
        );
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
            return text(opening, style);
        }

        public Component getClosingBracket(TextColor color) {
            return getClosingBracket(Style.style(color));
        }

        public Component getClosingBracket(Style style) {
            return text(opening, style);
        }

        // Default color, default style, preview brackets
        public Component getPreview(String guildName, TextColor primary, TextColor secondary) {
            return getOpeningBracket(secondary)
                    .append(text(guildName, primary))
                    .append(getClosingBracket(secondary));
        }
    }

    @RequiredArgsConstructor
    @Getter
    public enum Color {
        DEFAULT("default") {
            @Override
            public Component apply(String[] fullName,
                                   TextColor primary,
                                   TextColor secondary,
                                   Style bracket,
                                   Style text
            ) {
                return text()
                        .append(text(
                                fullName[OPENING_BRACKET],
                                bracket.color(secondary)
                        ))
                        .append(text(
                                fullName[GUILD_NAME],
                                text.color(primary)
                        ))
                        .append(text(
                                fullName[CLOSING_BRACKET],
                                bracket.color(secondary)
                        ))
                        .build();
            }
        },

        ALTERNATE("alternate") {
            @Override
            public Component apply(String[] fullName,
                                   TextColor primary,
                                   TextColor secondary,
                                   Style bracket,
                                   Style text
            ) {
                // Supplier that flips the color used everytime it's called
                Supplier<TextColor> colorProvider = new Supplier<>() {
                    boolean isPrimary = false;

                    @Override
                    public TextColor get() {
                        isPrimary = !isPrimary;
                        return isPrimary ? primary : secondary;
                    }
                };

                var builder = text();

                builder.append(text(
                        fullName[OPENING_BRACKET],
                        bracket.color(colorProvider.get())
                ));

                for (var c: fullName[GUILD_NAME].toCharArray()) {
                    builder.append(text(
                            c,
                            text.color(colorProvider.get())
                    ));
                }

                builder.append(text(
                        fullName[CLOSING_BRACKET],
                        bracket.color(colorProvider.get())
                ));

                return builder.build();
            }
        },

        GRADIENT_2COLORS("gradient2") {
            @Override
            public Component apply(String[] fullName,
                                   TextColor primary,
                                   TextColor secondary,
                                   Style bracket,
                                   Style text
            ) {
                return Guilds.createNameGradient(
                        1,
                        fullName,
                        primary,
                        secondary,
                        bracket,
                        text
                );
            }
        },
        GRADIENT_3COLORS("gradient3") {
            @Override
            public Component apply(String[] fullName,
                                   TextColor primary,
                                   TextColor secondary,
                                   Style bracket,
                                   Style text
            ) {
                return Guilds.createNameGradient(
                        2,
                        fullName,
                        primary,
                        secondary,
                        bracket,
                        text
                );
            }
        },
        GRADIENT_4COLORS("gradient4") {
            @Override
            public Component apply(String[] fullName,
                                   TextColor primary,
                                   TextColor secondary,
                                   Style bracket,
                                   Style text
            ) {
                return Guilds.createNameGradient(
                        3,
                        fullName,
                        primary,
                        secondary,
                        bracket,
                        text
                );
            }
        },
        ;

        @Getter
        private final String key;

        // Default brackets, default style, preview color
        public Component getPreview(String guildName,
                                    TextColor primary,
                                    TextColor secondary
        ) {
            return apply(
                    new String[]{
                            Bracket.DEFAULT.getOpening(),
                            guildName,
                            Bracket.DEFAULT.getClosing()
                    },

                    primary,
                    secondary,
                    Stylee.DEFAULT.getBracketStyle(),
                    Stylee.DEFAULT.getNameStyle()
            );
        }

        /**
         * Formats a guild's display name
         *
         * @param fullName The full name array containing the opening bracket,
         *                 the guild's name, and the closing bracket. Use the
         *                 {@link #CLOSING_BRACKET}, {@link #GUILD_NAME} and
         *                 {@link #OPENING_BRACKET} constants to access their
         *                 respective values.
         *
         * @param primary The guild's primary color.
         * @param secondary The guild's secondary color.
         * @param bracket The style to use for the brackets
         * @param text The style to use for the guild's name itself
         *
         * @return The formatted name
         */
        public abstract Component apply(String[] fullName,
                                        TextColor primary,
                                        TextColor secondary,
                                        Style bracket,
                                        Style text
        );
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

        private final String key;
        private final Style bracketStyle, nameStyle;

        // Default brackets, default color, preview styles
        public Component getPreview(String guildName, TextColor primary, TextColor secondary) {
            return text(Bracket.DEFAULT.getOpening(), bracketStyle.color(secondary))
                    .append(text(guildName, nameStyle.color(primary)))
                    .append(text( Bracket.DEFAULT.getClosing(), bracketStyle.color(secondary)));
        }
    }

}