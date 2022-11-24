package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.script.Script;
import net.forthecrown.core.script.ScriptManager;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriters;
import org.jetbrains.annotations.Nullable;

import javax.script.ScriptException;
import java.nio.file.Path;

public class CommandScripts extends FtcCommand {
    public CommandScripts() {
        super("Scripts");

        setAliases("script");
        setPermission(Permissions.ADMIN);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Scripts
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                // /script eval <JavaScript>
                .then(literal("eval")
                        .then(argument("input", StringArgumentType.greedyString())
                                .executes(this::eval)
                        )
                )

                // /script run <script> [method]
                .then(literal("run")
                        .then(argument("script", Arguments.SCRIPT)
                                .executes(c -> run(c, null))

                                .then(argument("func", StringArgumentType.string())
                                        .executes(c -> {
                                            String func = c.getArgument(
                                                    "func",
                                                    String.class
                                            );

                                            return run(c, func);
                                        })
                                )
                        )
                )

                // /script list
                .then(literal("list")
                        .executes(c -> {
                            var writer = TextWriters.newWriter();
                            var scripts = ScriptManager.getInstance()
                                    .findExistingScripts();

                            var it = scripts.listIterator();

                            while (it.hasNext()) {
                                var next = it.next();
                                int index = it.nextIndex();

                                writer.formattedLine(
                                        "&7{0, number}) &r{1}",
                                        index, next
                                );
                            }

                            c.getSource().sendMessage(writer);
                            return 0;
                        })
                )

                // /script delete <script>
                .then(literal("delete")
                        .then(argument("script", Arguments.SCRIPT)
                                .executes(this::delete)
                        )
                );
    }

    private int delete(CommandContext<CommandSource> c) {
        String script = c.getArgument("script", String.class);

        Path path = ScriptManager.getInstance()
                .getScriptFile(script);

        PathUtil.safeDelete(path)
                .resultOrPartial(FTC.getLogger()::error);

        c.getSource().sendAdmin(
                Text.format("Deleting script '{0}'", script)
        );
        return 0;
    }

    private int eval(CommandContext<CommandSource> c)
            throws CommandSyntaxException
    {
        String input = c.getArgument("input", String.class);

        try {
            var engine = ScriptManager.getInstance()
                    .createEngine("command-script", "");

            engine.eval(input);
        } catch (ScriptException exc) {
            throw Exceptions.format(
                    "Error running script: {0}",
                    exc.getMessage()
            );
        }

        c.getSource()
                .sendAdmin("Successfully ran script");

        return 0;
    }

    private int run(CommandContext<CommandSource> c,
                    @Nullable String method
    ) throws CommandSyntaxException {
        String scriptName = c.getArgument("script", String.class);
        var script = Script.read(scriptName);
        Object result = null;

        if (script.error().isPresent()) {
            throw Exceptions.format(
                    "Error evaluation script '{0}': {1}",
                    scriptName,
                    script.error().get()
            );
        } else if (script.result().isPresent()) {
            result = script.result().get();
        }

        if (method != null) {
            if (!script.hasMethod(method)) {
                throw Exceptions.format(
                        "Script '{0}' does not have method '{1}'",
                        scriptName,
                        method
                );
            }

            if (script.error().isPresent()) {
                throw Exceptions.format(
                        "Couldn't run '{0}' in '{1}': {2}",
                        method,
                        scriptName,
                        script.error().get()
                );
            } else if (script.result().isPresent()) {
                result = script.result().get();
            }
        }

        if (method != null) {
            c.getSource().sendAdmin(
                    Text.format("Successfully ran {0} in script {1}, result={2}",
                            method,
                            scriptName,
                            result
                    )
            );
        } else {
            c.getSource().sendAdmin(
                    Text.format("Successfully ran script {0}, result={1}",
                            scriptName,
                            result
                    )
            );
        }

        return 0;
    }
}