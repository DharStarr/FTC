package net.forthecrown.core.script;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.utils.io.PathUtil;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.Logger;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

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

        //Set the language to ECMA Script 6 mode
        System.setProperty("nashorn.args.prepend", "--language=es6");

        // Save default scripts
        try {
            PathUtil.saveJarPath("scripts", directory, false);
        } catch (IOException exc) {
            LOGGER.error("Couldn't save default scripts! {}", exc);
        }
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
                .orElseGet(ObjectLists::emptyList)
                .stream()
                .filter(s -> s.endsWith(".js"))
                .collect(Collectors.toList());
    }

    public NashornScriptEngine createEngine(String name, String... args) {
        var engine = (NashornScriptEngine) factory.getScriptEngine(
                args,
                getClassLoader(),
                className -> true
        );

        ScriptsBuiltIn.populate(name, engine);
        return engine;
    }

    private ClassLoader getClassLoader() {
        return FTC.getPlugin()
                .getClass()
                .getClassLoader();
    }

    public Path getScriptFile(String name) {
        return directory.resolve(ensureSuffix(name));
    }

    private String ensureSuffix(String name) {
        Validate.isTrue(name.endsWith(".js"),
                "Script name '%s' does not have .js suffix", name
        );
        return name;
    }
}