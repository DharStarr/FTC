package net.forthecrown.core.script2;

import lombok.Getter;

@Getter
public class ScriptExecutionException extends RuntimeException {
    private final Script script;
    private final String method;

    public ScriptExecutionException(Script script,
                                    String method,
                                    Throwable cause
    ) {
        super(
                String.format(
                        "Couldn't execute '%s' in '%s' reason: %s",
                        method,
                        script.getFile(),
                        cause.getMessage()
                ),
                cause
        );

        this.script = script;
        this.method = method;
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[0];
    }
}