package net.forthecrown.core.script;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.utils.io.PathUtil;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.Logger;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ScriptManager {
    private static final Logger LOGGER = FTC.getLogger();

    @Getter
    private static final ScriptManager instance = new ScriptManager();

    @Getter
    private final Path directory;

    @Getter
    private final NashornScriptEngineFactory
            factory = new NashornScriptEngineFactory();

    public ScriptManager() {
        this.directory = PathUtil.getPluginDirectory("scripts");
    }

    public boolean isExistingScript(String script) {
        if (!script.endsWith(".js")) {
            return false;
        }

        var path = getScriptFile(script);
        return Files.exists(path);
    }

    public List<String> findExistingScripts() {
        return PathUtil.findAllFiles(directory, true, false)
                .resultOrPartial(LOGGER::error)
                .orElseGet(ObjectLists::emptyList);
    }

    public ScriptResult readAndRunScript(String name, String... args) {
        Path file = getScriptFile(name);

        if (!Files.exists(file)) {
            return ScriptResult.error(
                    "Script '%s' does not exist",
                    name
            );
        }

        try {
            var reader = Files.newBufferedReader(file);
            NashornScriptEngine engine = (NashornScriptEngine) factory.getScriptEngine(
                    args,
                    getClassLoader(),
                    className -> true
            );

            ScriptsBuiltIn.populate(name, engine);

            try {
                var value = engine.eval(reader);
                return ScriptResult.success(engine, value);
            } catch (ScriptException exc) {
                return ScriptResult.error(engine, exc.getMessage());
            }

        } catch (IOException e) {
            LOGGER.error("IO exception reading script:", e);

            return ScriptResult.error(
                    "IO Exception during file read: '%s'",
                    e.getMessage()
            );
        }
    }

    private ClassLoader getClassLoader() {
        return FTC.getPlugin()
                .getClass()
                .getClassLoader();
    }

    private Path getScriptFile(String name) {
        return directory.resolve(ensureSuffix(name));
    }

    private String ensureSuffix(String name) {
        Validate.isTrue(name.endsWith(".js"),
                "Script name '%s' does not have .js suffix", name
        );
        return name;
    }
}