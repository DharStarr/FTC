package net.forthecrown.pirates.grappling;

import com.google.gson.JsonElement;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.squire.Squire;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.user.enums.Faction;
import net.forthecrown.utils.Worlds;
import org.bukkit.Location;

import java.util.*;

public class GhParkour extends AbstractJsonSerializer {

    public static final Location EXIT = new Location(Worlds.VOID, 1, 1, 1);
    public static final GhComparator COMPARATOR = new GhComparator();

    private final Map<String, GhLevelData> byName = new HashMap<>();
    private final List<GhLevelData> orderedList = new ArrayList<>();

    public GhParkour() {
        super("parkour_data");

        Registries.NPCS.register(Squire.createPiratesKey("gh_jack"), (player, entity) -> {
            CrownUser user = UserManager.getUser(player);

            if(user.getBranch() != Faction.PIRATES) throw FtcExceptionProvider.notPirate();
            sort();

            GhLevelSelector.SELECTOR_MENU.open(user);
        });

        reload();
    }

    public GhLevelData byName(String s){
        return byName.get(s);
    }

    public void add(GhLevelData data){
        add0(data);
        sort();
    }

    private void add0(GhLevelData data) {
        byName.put(data.getName(), data);
        orderedList.add(data);
    }

    public void resetProgress(UUID uuid) {
        byName.values().forEach(d -> d.uncomplete(uuid));
    }

    public void sort() {
        orderedList.sort(COMPARATOR);
        GhLevelSelector.recreateSelector();
    }

    public boolean isFirstUncompleted(UUID uuid, GhLevelData data) {
        if(data.hasCompleted(uuid)) return false;

        for (GhLevelData level : orderedList) {
            if (level.getNextLevel() == null) continue;
            if (!level.getNextLevel().equalsIgnoreCase(data.getName())) continue;

            return data.hasCompleted(uuid);
        }

        return false;
    }

    public Set<String> keySet() {
        return byName.keySet();
    }

    public Collection<GhLevelData> values() {
        return byName.values();
    }

    public Set<Map.Entry<String, GhLevelData>> entrySet() {
        return byName.entrySet();
    }

    public List<GhLevelData> getOrderedList() {
        return orderedList;
    }

    @Override
    protected void save(JsonBuf json) {
        for (GhLevelData d: orderedList) {
            json.add(d.getName(), d.serialize());
        }
    }

    @Override
    protected void reload(JsonBuf json) {
        byName.clear();
        orderedList.clear();

        for (Map.Entry<String, JsonElement> e: json.entrySet()) {
            add0(new GhLevelData(e.getKey(), e.getValue()));
        }

        orderedList.sort(COMPARATOR);
    }
}