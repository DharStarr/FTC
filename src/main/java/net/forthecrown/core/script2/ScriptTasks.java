package net.forthecrown.core.script2;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.Tasks;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ScriptTasks {
    private final Script script;
    private final List<TaskWrapper> tasks = new ObjectArrayList<>();

    void close() {
        for (var t: tasks) {
            Tasks.cancel(t.task);
        }

        tasks.clear();
    }

    interface TaskCallback {
        void run(Script script);
    }

    record ByNameCallback(String method) implements TaskCallback {
        @Override
        public void run(Script script) {
            script.invoke(method);
        }
    }

    @Getter
    @RequiredArgsConstructor
    static class TaskWrapper implements Runnable {
        private final TaskCallback callback;
        private final ScriptTasks tasks;

        private BukkitTask task;

        @Override
        public void run() {
            callback.run(tasks.getScript());
        }
    }
}