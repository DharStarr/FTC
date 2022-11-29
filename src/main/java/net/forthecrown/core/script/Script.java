package net.forthecrown.core.script;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import net.forthecrown.core.FTC;
import org.apache.logging.log4j.Logger;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.openjdk.nashorn.internal.runtime.ScriptFunction;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Objects;
import java.util.Optional;

@With(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Script {
    private static final Logger LOGGER = FTC.getLogger();

    /** Script's filename within the 'scripts' directory */
    @Getter
    private final String name;

    /** Engine this script is using, unique to the script */
    private final NashornScriptEngine engine;

    /** 'this' represents script's self instance */
    private final ScriptObjectMirror scriptHandle;

    /** Last result returned by script evaluation/invocation */
    private final Object lastResult;

    /** Last error thrown by the script */
    private final Throwable lastError;

    /* ---------------------------- CONSTRUCTORS ---------------------------- */

    private Script(String name) {
        this(name, null, null, null, null);
    }

    /* ------------------------ STATIC CONSTRUCTORS ------------------------- */

    /**
     * Creates an empty script object with the given name
     * @param name The name of the script
     * @return The created script
     * @see #isEmpty()
     */
    public static Script of(String name) {
        return new Script(name);
    }

    /**
     * Reads the script file with the given name.
     * <p>
     * Script names are relative to the <code>plugins/ForTheCrown/scripts</code>
     * directory, as an example, a script used by a challenge might be called,
     * <code>challenges/on_challenge.js</code>. They must always include the
     * '.js' file extension as well
     *
     * @param scriptFile The script's name
     *
     * @return The read script, if during reading or initial script evaluation,
     *         an error occurs, the result will contain an error
     */
    public static Script read(String scriptFile) {
        return of(scriptFile).load();
    }

    /**
     * Reads the given script file and invokes the given function with
     * the given arguments.
     *
     * @see #read(String)
     *
     * @param scriptFile The script's name
     * @param function The name of the function to call
     * @param args The argument to give to the function
     *
     * @return The read and created script. If an error occurs during reading or
     *         during the initial script evaluation, then the method won't be
     *         run and the resulting script object will contain an error
     */
    public static Script run(String scriptFile, String function, Object... args) {
        return read(scriptFile).invoke(function, args);
    }

    /* ------------------------------ METHODS ------------------------------- */

    /**
     * Invokes the given method with the given arguments.
     * <p>
     * If no {@link #engine()} is present, this method
     * will simply return itself, as there is no engine to
     * run the script in that case.
     * <p>
     * The returned script will be a copy of this script,
     * with either the result of the method invocation or
     * an error, if it failed in any way.
     * <p>
     * Note: {@link NoSuchMethodException} are not logged
     * by themselves, this is because there are various
     * usages of this method that have the given method
     * as an 'optional' implementation, meaning the usage
     * case doesn't care if the method is or isn't implemented.
     *
     * @param method The name of the method to invoke
     * @param args The arguments to give the method
     *
     * @return The resulting script copy
     */
    public Script invoke(String method, Object... args) {
        if (engine == null) {
            return this;
        }

        try {
            var result = engine.invokeMethod(scriptHandle, method, args);

            return withLastResult(result)
                    .withLastError(null);
        } catch (ScriptException exc) {
            LOGGER.error("Error running script {}, function {}",
                    name, method, exc
            );

            return withLastError(exc)
                    .withLastResult(null);
        } catch (NoSuchMethodException exc) {

            return withLastError(exc)
                    .withLastResult(null);
        }
    }

    /**
     * Loads this script's engine and {@link #scriptHandle} instance.
     * <p>
     * If the script's file doesn't exist, then this method returns
     * an error script with {@link NoSuchFileException} if the script's
     * file doesn't exist.
     * <p>
     * If the script fails to be evaluated, or fails to be read from
     * the script file, then a failure result is returned, either with
     * {@link IOException} or {@link ScriptException}.
     * <p>
     * If the script is loaded and then successfully evaluated, then
     * a complete result with the engine, {@link #scriptHandle} instance
     * and 'returned result' is returned.
     * <p>
     * Note: The result returned by this will always be empty, meaning
     * it doesn't contain the current script instance's result, error
     * or {@link #scriptHandle} instance
     *
     * @return The loaded script, or an error result if
     *         script failed to load or be evaluated.
     */
    public Script load() {
        var result = of(name);

        var path = ScriptManager.getInstance()
                .getScriptFile(name);

        if (!Files.exists(path)) {
            return result.withLastError(
                    new NoSuchFileException(path.toString())
            );
        }

        try {
            var engine = ScriptManager.getInstance()
                    .createEngine(name);

            var reader = Files.newBufferedReader(path);
            var eval = engine.eval(reader);

            return result.withEngine(engine)
                    .withScriptHandle(
                            (ScriptObjectMirror) engine.getBindings(ScriptContext.ENGINE_SCOPE)
                    )
                    .withLastResult(eval);
        }
        catch (IOException exc) {
            LOGGER.error("Couldn't read script file {}", path, exc);
            return result.withLastError(exc);
        }
        catch (ScriptException exc) {
            LOGGER.error("Error evaluating script {}", name, exc);
            return result.withLastError(exc);
        }
    }

    /**
     * Tests if this script has the given method.
     * <p>
     * If this script has no {@link #scriptHandle} or {@link #isEmpty()} returns
     * true, then will return false, as that means there's no script handle in
     * place to query for the method.
     *
     * @param method The name of the method to look for
     * @return True, if the script contains the given method, false otherwise
     */
    public boolean hasMethod(String method) {
        if (scriptHandle == null) {
            return false;
        }

        var m = scriptHandle.get(method);

        if (m == null) {
            return false;
        }

        if (m instanceof ScriptObjectMirror mirror) {
            return mirror.isFunction();
        }

        return m instanceof ScriptFunction;
    }

    /**
     * Gets a method by its specified name.
     * <p>
     * This method will first call {@link #hasMethod(String)} to test if the
     * method actually exists at all. If that returns false, an empty optional
     * is returned, otherwise, an optional containing the function's wrapper
     * object is returned.
     *
     * @param name The name of the function to get
     *
     * @return An optional with the function's wrapper, or an empty optional, if
     *         a function with the given name does not exist
     */
    public Optional<ScriptObjectMirror> getMethod(String name) {
        if (!hasMethod(name)) {
            return Optional.empty();
        }

        return Optional.of((ScriptObjectMirror) scriptHandle.getMember(name));
    }

    /**
     * Tests if this script is 'empty' or not.
     * <p>
     * A script instance will be empty if it hasn't loaded a script file and
     * doesn't have an error.
     * @return True, if both the {@link #engine} and {@link #lastError} are
     *         unset, false otherwise
     */
    public boolean isEmpty() {
        return engine == null && lastError == null;
    }

    public Optional<NashornScriptEngine> engine() {
        return Optional.ofNullable(engine);
    }

    public Optional<Object> result() {
        return Optional.ofNullable(lastResult);
    }

    public Optional<Throwable> error() {
        return Optional.ofNullable(lastError);
    }

    public Optional<Boolean> resultAsBoolean() {
        return result().map(o -> {
            if (o instanceof Boolean b) {
                return b;
            }

            if (o instanceof Number num) {
                return num.longValue() != 0;
            }

            return Boolean.parseBoolean(String.valueOf(o));
        });
    }

    /* ------------------------- OBJECT OVERRIDES --------------------------- */

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Script script)) {
            return false;
        }

        return getName().equals(script.getName())
                && Objects.equals(scriptHandle, script.scriptHandle)
                && Objects.equals(lastResult, script.lastResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), scriptHandle, lastResult);
    }
}