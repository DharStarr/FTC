package net.forthecrown.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FtcUserHomes implements UserHomes, JsonSerializable, JsonDeserializable {
    public Map<String, Location> homes = new HashMap<>();
    private final FtcUser owner;

    public FtcUserHomes(FtcUser user){
        this.owner = user;

        checkOverLimit();
    }

    public void checkOverLimit(){ //Remove homes until under limit
        if(owner.isOp()) return;

        homes.entrySet().removeIf(e -> check());
    }

    private boolean check(){
        int max = owner.getHighestTierRank().tier.maxHomes;
        int current = size();

        return max < current;
    }

    public void saveInto(ConfigurationSection section){
        checkOverLimit();

        for (Map.Entry<String, Location> e: homes.entrySet()){
            section.set(e.getKey(), e.getValue());
        }
    }

    public void loadFrom(ConfigurationSection section){
        homes.clear();
        for (String s: section.getKeys(false)){
            homes.put(s, section.getLocation(s));
        }

        checkOverLimit();
    }

    @Override
    public void clear() {
        homes.clear();
    }

    @Override
    public int size(){
        return homes.size();
    }

    @Override
    public boolean contains(String name){
        return homes.containsKey(name);
    }

    @Override
    public boolean canMakeMore(){
        if(owner.isOp()) return true;

        int currentHomes = size();
        int maxHomes = owner.getHighestTierRank().tier.maxHomes;

        return currentHomes <= maxHomes;
    }

    @Override
    public boolean isEmpty() {
        checkOverLimit();
        return homes.isEmpty();
    }

    @Override
    public Map<String, Location> getHomes() {
        checkOverLimit();
        return homes;
    }

    @Override
    public CrownUser getUser() {
        return owner;
    }

    @Override
    public Set<String> getHomeNames(){
        checkOverLimit();
        return homes.keySet();
    }

    @Override
    public Collection<Location> getHomeLocations(){
        checkOverLimit();
        return homes.values();
    }

    @Override
    public void set(String name, Location location){
        homes.put(name, location);
    }

    @Override
    public void remove(String name){
        homes.remove(name);
    }

    @Override
    public Location get(String name){
        return homes.get(name);
    }

    @Override
    public JsonObject serialize() {
        if(homes.isEmpty()) return null;

        JsonBuf json = JsonBuf.empty();

        for (Map.Entry<String, Location> e: homes.entrySet()){
            json.addLocation(e.getKey(), e.getValue());
        }

        return json.nullIfEmpty();
    }

    @Override
    public void deserialize(JsonElement element) {
        homes.clear();

        if(element == null) return;
        JsonObject json = element.getAsJsonObject();

        for (Map.Entry<String, JsonElement> e: json.entrySet()){
            homes.put(e.getKey(), JsonUtils.readLocation(e.getValue().getAsJsonObject()));
        }
    }
}
