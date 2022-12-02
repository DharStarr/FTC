package net.forthecrown.core.script2;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import jdk.dynalink.beans.StaticClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import net.forthecrown.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.openjdk.nashorn.internal.runtime.Context;
import org.openjdk.nashorn.internal.runtime.ScriptFunction;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ScriptEvents {
    private final Script script;
    private final List<ExecutorWrapper> wrappers = new ObjectArrayList<>();

    public void register(Object event, Object listener) {
        register(event, null, listener, EventPriority.NORMAL, false);
    }

    public void register(Object event, Object thiz, Object listener) {
        register(event, thiz, listener, EventPriority.NORMAL, false);
    }

    public void register(Object event,
                         Object thiz,
                         Object listener,
                         EventPriority priority
    ) {
        register(event, thiz, listener, priority, false);
    }

    public void register(Object event,
                         Object thiz,
                         Object listener,
                         EventPriority priority,
                         boolean ignoreCancelled
    ) {
        ScriptListenerHandle handle;
        Class<? extends Event> type;

        if (event instanceof StaticClass c) {
            event = c.getRepresentedClass();
        } else if (event instanceof String s) {
            try {
                event = Class.forName(s, true, getClass().getClassLoader());
            } catch (ClassNotFoundException exc) {
                throw new IllegalStateException(exc);
            }
        }

        if (event instanceof Class<?> c) {
            if (!Event.class.isAssignableFrom(c)) {
                throw Util.newException("Class '%s' is not an event class!", c);
            }

            type = (Class<? extends Event>) c;
        } else {
            throw Util.newException(
                    "Invalid event type input: '%s'",
                    event
            );
        }

        if (listener instanceof ScriptFunction f) {
            listener = ScriptObjectMirror.wrap(f, Context.getGlobal());
        }

        if (listener instanceof String s) {
            handle = new ByNameHandle(s);
        } else if (listener instanceof ScriptObjectMirror m) {
            if (!m.isFunction() && !m.isStrictFunction()) {
                throw Util.newException(
                        "Input '%s' is not a function nor a function name",
                        listener
                );
            }

            handle = new FunctionRefHandle(
                    m,
                    thiz == null ? script.getMirror() : thiz
            );
        } else {
            throw Util.newException(
                    "Invalid type of listener: %s",
                    listener
            );
        }

        ExecutorWrapper wrapper = new ExecutorWrapper(handle, type, this);
        wrappers.add(wrapper);

        Bukkit.getPluginManager()
                .registerEvent(
                        type,
                        wrapper,
                        priority,
                        wrapper,
                        FTC.getPlugin(),
                        ignoreCancelled
                );
    }

    public void unregister(Object listener) {
        if (listener instanceof String s) {
            wrappers.removeIf(wrapper -> {
                if (!(wrapper.handle instanceof ByNameHandle h
                        && h.methodName().equalsIgnoreCase(s))
                ) {
                    return false;
                }

                HandlerList.unregisterAll(wrapper);
                return true;
            });

            return;
        }

        if (listener instanceof ScriptFunction f) {
            listener = ScriptObjectMirror.wrap(f, Context.getGlobal());
        }

        if (listener instanceof ScriptObjectMirror m
                && (m.isStrictFunction() || m.isFunction())
        ) {
            wrappers.removeIf(wrapper -> {
                if (wrapper.handle instanceof FunctionRefHandle h
                        && h.mirror.equals(m)
                ) {
                    HandlerList.unregisterAll(wrapper);
                    return true;
                }

                return false;
            });

            return;
        }

        throw Util.newException(
                "Invalid type of input for unregister() method: %s",
                listener
        );
    }

    void close() {

    }

    @Getter
    @RequiredArgsConstructor
    static class ExecutorWrapper implements EventExecutor, Listener {
        private final ScriptListenerHandle handle;
        private final Class<? extends Event> type;
        private final ScriptEvents events;

        @Override
        public void execute(@NotNull Listener listener, @NotNull Event event)
                throws EventException
        {
            if (listener instanceof Cancellable c
                    && c.isCancelled()
            ) {
                return;
            }

            if (!type.isAssignableFrom(event.getClass())) {
                return;
            }

            handle.invoke(events.getScript(), event);
        }
    }

    interface ScriptListenerHandle {
        void invoke(Script script, Event event) throws ScriptExecutionException;
    }

    record ByNameHandle(String methodName) implements ScriptListenerHandle {
        @Override
        public void invoke(Script script, Event event) {
            script.invoke(methodName, event);
        }
    }

    record FunctionRefHandle(ScriptObjectMirror mirror, Object thiz)
            implements ScriptListenerHandle
    {
        @Override
        public void invoke(Script script, Event event) {
            try {
                mirror.call(thiz, event);
            } catch (Exception e) {
                throw new ScriptExecutionException(
                        script, "<method reference>", e
                );
            }
        }
    }
}