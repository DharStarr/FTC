package net.forthecrown.core.script;

import lombok.experimental.UtilityClass;

public @UtilityClass class Scripts {
    public ScriptResult read(String scriptFile, String... args) {
        return ScriptManager.getInstance()
                .readAndRunScript(scriptFile, args)
                .logIfError();
    }

    public ScriptResult run(String scriptFile,
                            String function,
                            Object... args
    ) {
        return read(scriptFile)
                .invoke(function, args)
                .logIfError();
    }
}