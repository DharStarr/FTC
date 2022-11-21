package net.forthecrown.core.script;

import lombok.experimental.UtilityClass;

public @UtilityClass class Scripts {
    public Script read(String scriptFile) {
        return Script.of(scriptFile)
                .load()
                .logError();
    }

    public Script run(String scriptFile,
                            String function,
                            Object... args
    ) {
        return Script.of(scriptFile)
                .load()
                .invoke(function, args)
                .logError();
    }
}