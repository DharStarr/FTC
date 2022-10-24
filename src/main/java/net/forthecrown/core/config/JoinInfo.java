package net.forthecrown.core.config;

import lombok.experimental.UtilityClass;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;

@ConfigData(filePath = "joininfo.json")
public @UtilityClass class JoinInfo {
    public boolean endVisible;
    public boolean visible;

    public Component info = Component.empty();
    public Component endInfo = Component.empty();

    public Component display() {
        TextWriter writer = TextWriters.newWriter();

        if (visible) {
            writer.line(info);
        }

        if (endVisible) {
            writer.line(endInfo);
        }

        return writer.asComponent();
    }
}