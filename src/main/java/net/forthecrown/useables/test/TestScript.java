package net.forthecrown.useables.test;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.core.script.Script;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.*;
import net.forthecrown.user.Users;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class TestScript extends UsageTest {
    public static final UsageType<TestScript> TYPE = UsageType.of(TestScript.class)
            .setSuggests(Arguments.SCRIPT::listSuggestions);

    private String script;

    public TestScript(String script) {
        super(TYPE);
        this.script = script;
    }

    @Override
    public @Nullable Component displayInfo() {
        return Component.text(
                String.format("'%s'", script)
        );
    }

    @Override
    public @Nullable Tag save() {
        return StringTag.valueOf(script);
    }

    @Override
    public boolean test(Player player, CheckHolder holder) {
        var script = Script.read(this.script);

        if (!script.hasMethod("test")) {
            return true;
        }

        return script.invoke("test", Users.get(player))
                .resultAsBoolean()
                .orElse(false);
    }

    @Override
    public @Nullable Component getFailMessage(Player player, CheckHolder holder) {
        var script = Script.read(this.script);

        if (!script.hasMethod("getFailMessage")) {
            return null;
        }

        var result = script.invoke("getFailMessage", Users.get(player))
                .result();

        var obj = result.get();
        return Text.valueOf(obj);
    }

    @Override
    public void postTests(Player player, CheckHolder holder) {
        var script = Script.read(this.script);

        if (!script.hasMethod("onTestsPassed")) {
            return;
        }

        script.invoke("onTestsPassed", Users.get(player));
    }

    @UsableConstructor(ConstructType.PARSE)
    public static TestScript parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new TestScript(Arguments.SCRIPT.parse(reader));
    }

    @UsableConstructor(ConstructType.TAG)
    public static TestScript readTag(Tag tag) {
        return new TestScript(tag.getAsString());
    }
}