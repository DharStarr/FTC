package net.forthecrown.log;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RequiredArgsConstructor
class QueryResultBuilder
        implements Consumer<LogEntry>, Predicate<LogEntry>
{
    // Linked list style implementation of
    // node list, addition to this is faster than
    // to an array list since no array reallocation
    // has to be performed
    private EntryNode head;
    private EntryNode tail;

    @Getter
    private int found = 0;

    @Getter
    private final LogQuery query;

    @Override
    public void accept(LogEntry entry) {
        found++;

        if (head == null) {
            head = new EntryNode(entry);
            tail = head;
            return;
        }

        EntryNode node = new EntryNode(entry);
        node.next = head;
        head.previous = node;

        this.head = node;
    }

    @Override
    public boolean test(LogEntry entry) {
        return query.test(entry);
    }

    public boolean hasFoundEnough() {
        if (query.getMaxResults() == -1) {
            return false;
        }

        return found >= query.getMaxResults();
    }

    public List<LogEntry> build() {
        List<LogEntry> entries = new ObjectArrayList<>(found);
        EntryNode node = tail;

        while (node != null) {
            entries.add(node.getEntry());
            node = node.getPrevious();
        }

        return entries;
    }

    @Getter
    @RequiredArgsConstructor
    private static class EntryNode {
        private final LogEntry entry;
        private EntryNode next;
        private EntryNode previous;
    }
}