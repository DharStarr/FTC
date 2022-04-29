package net.forthecrown.regions;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.commands.CommandHome;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.ComponentWriter;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.chat.TimePrinter;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RegionResidency {
    public static final long CUT_OFF_TIMESTAMP = Util.make(() -> {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0L);
        calendar.set(Calendar.YEAR, 2000);

        return calendar.getTimeInMillis();
    });

    @Getter private final PopulationRegion region;

    @Getter
    private final Map<UUID, ResEntry> entries = new Object2ObjectOpenHashMap<>();

    public ResEntry getEntry(UUID uuid) {
        return entries.computeIfAbsent(uuid, uuid1 -> new ResEntry());
    }

    public Tag save() {
        ListTag list = new ListTag();

        for (var e: entries.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("resident", e.getKey());

            ResEntry entry = e.getValue();

            if (entry.isDirectResident()) {
                tag.putLong("directMoveIn", entry.directMoveIn);
            }

            if (!FtcUtils.isNullOrEmpty(entry.homes)) {
                CompoundTag homes = new CompoundTag();
                entry.homes.forEach(homes::putLong);

                tag.put("homes", homes);
            }

            list.add(tag);
        }

        return list;
    }

    public void load(ListTag t) {
        for (Tag listT: t) {
            CompoundTag tag = (CompoundTag) listT;

            UUID resident = tag.getUUID("resident");
            ResEntry entry = new ResEntry();

            entry.directMoveIn = tag.getLong("directMoveIn");

            if(tag.contains("homes")) {
                CompoundTag homesTag = tag.getCompound("homes");

                for (var e: homesTag.tags.entrySet()) {
                    entry.homesSafe().put(e.getKey(), ((LongTag) e.getValue()).getAsLong());
                }
            }

            entries.put(resident, entry);
        }
    }

    private void clearEmpty() {
        entries.entrySet().removeIf(entry -> {
            ResEntry e = entry.getValue();
            boolean notDirect = !e.isDirectResident();
            boolean noHomes = !e.hasHomes();

            return noHomes && notDirect;
        });
    }

    public void clear() {
        entries.clear();
    }

    public boolean isEmpty() {
        clearEmpty();
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    private static final Logger LOGGER = Crown.logger();

    public void write(ComponentWriter writer) {
        if(isEmpty()) return;

        writer.write(
                Component.translatable("regions.reside.header." + (size() == 1 ? "single" : "multiple"),
                        NamedTextColor.GRAY,
                        Component.text(size(), NamedTextColor.YELLOW),
                        getRegion()
                                .displayName()
                                .colorIfAbsent(NamedTextColor.YELLOW)
                )
        );

        int index = 0;

        for (var v: entries.entrySet()) {
            index++;

            CrownUser user = UserManager.getUser(v.getKey());
            ResEntry entry = v.getValue();
            String transKey = "regions.reside.format.";

            if (entry.isDirectResident() || (entry.homes != null && entry.homes.containsKey(CommandHome.DEFAULT))) {
                transKey += "direct.";
            }

            transKey += (user.isProfilePublic() ? "public" : "private");

            long time = entry.timeSinceMoveIn();

            if(Crown.inDebugMode()) {
                LOGGER.info("time: {}", time);
                LOGGER.info("directMoveIn: {}, homes: {}", entry.directMoveIn, entry.homes);
            }

            HoverEvent hover = null;

            if(Crown.inDebugMode()) {
                hover = Component.text("TimeStamp: " + time + ", formatted: ")
                        .append(FtcFormatter.formatDate(time))
                        .asHoverEvent();
            }

            writer.newLine();
            writer.write(Component.text(index + ") ", NamedTextColor.GOLD));
            writer.write(
                    Component.translatable(transKey,
                            NamedTextColor.GRAY,
                            user.nickDisplayName().color(NamedTextColor.YELLOW),

                            (time < 1 ?
                                    Component.translatable("regions.reside.unknownTime")
                                    : new TimePrinter(time).printBiggest()
                            )
                                    .hoverEvent(hover)
                                    .color(NamedTextColor.YELLOW)
                    )
            );
        }
    }

    public static class ResEntry {
        @Setter @Getter
        private long directMoveIn;

        private Object2LongMap<String> homes;

        public void addHome(String home, long timeStamp) {
            Object2LongMap<String> homes = homesSafe();
            homes.put(home, timeStamp);
        }

        public void removeHome(String home) {
            if(homes == null) return;
            homes.removeLong(home);
        }

        public boolean isDirectResident() {
            return directMoveIn != 0L;
        }

        public long timeSinceMoveIn() {
            long firstMoveIn = firstMoveIn();
            return firstMoveIn < CUT_OFF_TIMESTAMP ? -1 : TimeUtil.timeSince(firstMoveIn);
        }

        public long firstMoveIn() {
            if(directMoveIn > 0) {
                return directMoveIn;
            }

            if(!hasHomes()) {
                return -1;
            }

            return homes.values()
                    .longStream()
                    .min()
                    .orElse(-1);
        }

        public boolean hasHomes() {
            return homes != null && !homes.isEmpty();
        }

        private Object2LongMap<String> homesSafe() {
            return homes == null ? (homes = new Object2LongOpenHashMap<>()) : homes;
        }
    }
}