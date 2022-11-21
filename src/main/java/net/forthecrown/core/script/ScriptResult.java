package net.forthecrown.core.script;

import com.google.common.base.Strings;
import com.mojang.serialization.DataResult;
import lombok.Getter;
import lombok.With;
import net.forthecrown.core.FTC;
import org.apache.logging.log4j.Logger;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptException;
import java.util.Optional;
import java.util.function.Consumer;

@With
@Getter
public class ScriptResult {
    private static final Logger LOGGER = FTC.getLogger();

    private final NashornScriptEngine engine;
    private final Object result;
    private final String errorMessage;

    /* ---------------------------- CONSTRUCTORS ---------------------------- */

    public ScriptResult(NashornScriptEngine engine,
                        Object result,
                        String errorMessage
    ) {
        this.engine = engine;
        this.result = result;
        this.errorMessage = errorMessage;

        if (!hasEngine() && !hasError()) {
            throw new IllegalStateException(
                    "Either engine or error message must be given, " +
                            "both cannot be null"
            );
        }

        if (hasError() && hasResult()) {
            throw new IllegalArgumentException(
                    "Both error and result cannot be specified"
            );
        }
    }

    /* ------------------------ STATIC CONSTRUCTORS ------------------------- */

    public static ScriptResult error(String msg, Object... args) {
        return new ScriptResult(null, null, String.format(msg, args));
    }

    public static ScriptResult error(NashornScriptEngine engine,
                                     String msg,
                                     Object... args
    ) {
        return new ScriptResult(engine, null, String.format(msg, args));
    }

    public static ScriptResult success(NashornScriptEngine engine, Object res) {
        return new ScriptResult(engine, res, null);
    }

    public static ScriptResult success(NashornScriptEngine engine) {
        return new ScriptResult(engine, null, null);
    }

    /* ------------------------------ METHODS ------------------------------- */

    public ScriptResult invoke(String name, Object... args) {
        if (!hasEngine()) {
            return this;
        }

        try {
            var value = engine.invokeFunction(name, args);
            return withResult(value);
        }
        catch (ScriptException e) {
            LOGGER.error("Error running method {}", name, e);
            return error(engine, e.getMessage());
        }
        catch (NoSuchMethodException e) {
            return error(engine, "No such method: '%s'", name);
        }
    }

    public boolean hasError() {
        return !Strings.isNullOrEmpty(errorMessage);
    }

    public boolean isMissingMethod() {
        return hasError() && errorMessage.contains("No such method: '");
    }

    public boolean hasResult() {
        return result != null;
    }

    public boolean hasEngine() {
        return engine != null;
    }

    public ScriptResult logIfError() {
        return ifError(FTC.getLogger()::error);
    }

    public DataResult<NashornScriptEngine> toDataResult() {
        if (hasError()) {
            return DataResult.error(errorMessage);
        }

        if (hasEngine()) {
            return DataResult.success(engine);
        }

        // Shouldn't even be possible to reach
        throw new IllegalStateException("No engine and no error... how???");
    }

    public ScriptResult ifError(Consumer<String> consumer) {
        if (hasError()) {
            consumer.accept(errorMessage);
        }

        return this;
    }

    public Optional<Integer> resultAsInt() {
        if (!hasResult()) {
            return Optional.empty();
        }

        if (result instanceof Number number) {
            return Optional.of(number.intValue());
        }

        if (result instanceof String str) {
            return Optional.of(
                    Integer.parseInt(str)
            );
        }

        return Optional.empty();
    }

    public Optional<Boolean> resultAsBoolean() {
        if (!hasResult()) {
            return Optional.empty();
        }

        if (result instanceof Boolean b) {
            return Optional.of(b);
        }

        if (result instanceof Number number) {
            return Optional.of(number.longValue() != 0);
        }

        if (result instanceof String s) {
            return Optional.of(
                    Boolean.parseBoolean(s)
            );
        }

        return Optional.empty();
    }

    public Optional<Object> result() {
        if (!hasResult()) {
            return Optional.empty();
        }

        return Optional.of(result);
    }
}