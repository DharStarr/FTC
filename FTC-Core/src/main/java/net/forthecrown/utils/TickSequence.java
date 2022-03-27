package net.forthecrown.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TickSequence {
    protected final List<SequenceNode> nodes;
    protected int nodeIndex = 0;
    protected BukkitTask task;
    protected boolean running;

    public TickSequence(SequenceNode... nodes) {
        this(Arrays.asList(nodes));
    }

    public TickSequence(Collection<SequenceNode> nodes) {
        this.nodes = new ObjectArrayList<>(nodes);
    }

    public TickSequence() { this(Collections.EMPTY_LIST); }

    public TickSequence addNode(SequenceNode node) {
        this.nodes.add(node);
        return this;
    }

    public TickSequence addNode(Runnable runnable, int delay) {
        return addNode(new SequenceNode(runnable, delay));
    }

    public void start() {
        stop();
        startNext();
        running = true;
    }

    public void stop() {
        if(task == null || task.isCancelled()) return;
        task.cancel();
        task = null;
        running = false;
        nodeIndex = 0;
    }

    public boolean isRunning() {
        return running;
    }

    private void run() {
        SequenceNode node = nodes.get(nodeIndex++);

        try {
            node.runnable.run();
        } catch (Exception e) {
            Crown.logger().error("Couldn't run sequence node", e);
        }

        if(nodeIndex >= nodes.size()) {
            nodeIndex = 0;
            running = false;
            return;
        }

        startNext();
    }

    private void startNext() {
        task = Bukkit.getScheduler().runTaskLater(Crown.inst(), this::run, nodes.get(nodeIndex).delay);
    }

    public record SequenceNode(Runnable runnable, int delay) {
    }
}