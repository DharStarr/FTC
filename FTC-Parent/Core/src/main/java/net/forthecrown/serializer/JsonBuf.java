package net.forthecrown.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.math.BlockPos;
import net.forthecrown.utils.math.CrownBoundingBox;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

import static net.forthecrown.utils.JsonUtils.*;

/**
 * A JSON Buffer because this shit is too verbose on its own
 */
public class JsonBuf {

    private final JsonObject json;

    JsonBuf(JsonObject json) {
        this.json = json;
    }

    public static JsonBuf of(JsonObject json){
        return new JsonBuf(json);
    }

    public static JsonBuf empty(){
        return new JsonBuf(new JsonObject());
    }

    public void add(String name, JsonSerializable serializable) {
        json.add(name, serializable.serialize());
    }

    public <E extends Enum<E>> void addEnum(String name, E anum) {
        json.add(name, writeEnum(anum));;
    }

    public <E extends Enum<E>> E getEnum(String name, Class<E> clazz){
        return getEnum(name, clazz, null);
    }

    public <E extends Enum<E>> E getEnum(String name, Class<E> clazz, E def){
        if(missingOrNull(name)) return def;
        return readEnum(clazz, json.get(name));
    }

    public void addLocation(String name, Location location){
        json.add(name, writeLocation(location));
    }

    public Location getLocation(String name){
        return get(name, e -> readLocation(e.getAsJsonObject()));
    }

    public void addUUID(String name, UUID id){
        add(name, id.toString());
    }

    public UUID getUUID(String name){
        if(missingOrNull(name)) return null;
        return UUID.fromString(getString(name));
    }

    public void addKey(String name, Key key){
        json.add(name, writeKey(key));
    }

    public Key getKey(String name){
        return get(name, JsonUtils::readKey);
    }

    public void addRegion(String name, CrownBoundingBox box){
        json.add(name, writeRegion(box));
    }

    public CrownBoundingBox getRegion(String name){
        return get(name, e -> readRegion(e.getAsJsonObject()));
    }

    public void addItem(String name, ItemStack item){
        json.add(name, writeItem(item));
    }

    public ItemStack getItem(String name){
        if(missingOrNull(name)) return null;

        return readItem(get(name));
    }

    public <T> T get(String name, Function<JsonElement, T> function){
        if(missingOrNull(name)) return null;
        return function.apply(json.get(name));
    }

    public <T> Collection<T> getList(String name, Function<JsonElement, T> func){
        if(missingOrNull(name)) return null;

        Collection<T> list = new ArrayList<>();

        JsonArray array = getAsJsonArray(name);
        array.forEach(e -> list.add(func.apply(e)));
        
        return list;
    }
    
    public void addList(String name, Iterable<? extends JsonSerializable> list){
        addList(name, list, JsonSerializable::serialize);
    }
    
    public <T> void addList(String name, Iterable<T> iterable, Function<T, JsonElement> function){
        JsonArray array = new JsonArray();
        iterable.forEach(e -> array.add(function.apply(e)));

        json.add(name, array);
    }

    public BlockPos getPos(String name){
        if(missingOrNull(name)) return null;
        return get(name, BlockPos::of);
    }

    public String getString(String name){ return getString(name, null); }

    public String getString(String name, String def){
        if(missingOrNull(name)) return def;
        return json.get(name).getAsString();
    }

    public boolean getBool(String name){ return getBool(name, false); }

    public boolean getBool(String name, boolean def){
        if(missingOrNull(name)) return def;
        return get(name).getAsBoolean();
    }

    public long getLong(String name){ return getLong(name, 0L); }

    public long getLong(String name, long def){
        if(missingOrNull(name)) return def;
        return get(name).getAsLong();
    }

    public double getDouble(String name){ return getDouble(name, 0D); }

    public double getDouble(String name, double def){
        if(missingOrNull(name)) return def;
        return get(name).getAsDouble();
    }

    public float getFloat(String name){ return getFloat(name, 0f); }

    public float getFloat(String name, float def){
        if(missingOrNull(name)) return def;
        return get(name).getAsFloat();
    }

    public BigDecimal getBigDecimal(String name){ return getBigDecimal(name, null); }

    public BigDecimal getBigDecimal(String name, BigDecimal def){
        if(missingOrNull(name)) return def;
        return get(name).getAsBigDecimal();
    }

    public BigInteger getBigInt(String name){ return getBigInt(name, null); }

    public BigInteger getBigInt(String name, BigInteger def){
        if(missingOrNull(name)) return def;
        return get(name).getAsBigInteger();
    }

    public int getInt(String name){ return getInt(name, 0); }

    public int getInt(String name, int def){
        if(missingOrNull(name)) return def;
        return json.get(name).getAsInt();
    }

    public short getShort(String name){ return getShort(name, (short) 0); }

    public short getShort(String name, short def){
        if(missingOrNull(name)) return def;
        return json.get(name).getAsShort();
    }

    public byte getByte(String name){ return getByte(name, (byte) 0); }

    public byte getByte(String name, byte def){
        if(missingOrNull(name)) return def;
        return get(name).getAsByte();
    }

    public JsonBuf getBuf(String name){
        if(missingOrNull(name)) return null;
        return of(getAsJsonObject(name));
    }

    public void add(String name, JsonBuf buf){
        json.add(name, buf.json);
    }

    public boolean isEmpty(){
        return size() < 1;
    }

    public boolean missingOrNull(String name){
        return !has(name) || get(name) == null || get(name).isJsonNull();
    }

    public <T> void addMap(String name, Map<String, T> map, Function<T, JsonElement> func){
        addMap(name, map, Function.identity(), func);
    }

    public <K, V> void addMap(String name, Map<K, V> map, Function<K, String> keyFunc, Function<V, JsonElement> valueFunc) {
        JsonObject jsonMap = new JsonObject();

        for (Map.Entry<K, V> e : map.entrySet()) {
            jsonMap.add(keyFunc.apply(e.getKey()), valueFunc.apply(e.getValue()));
        }

        json.add(name, jsonMap);
    }

    public <K, V> Map<K, V> getMap(String name, Function<String, K> keyFunc, Function<JsonElement, V> valueFunc){
        if(missingOrNull(name)) return null;

        JsonObject json = getAsJsonObject(name);
        Map<K, V> result = new HashMap<>();

        for (Map.Entry<String, JsonElement> e: json.entrySet()){
            result.put(keyFunc.apply(e.getKey()), valueFunc.apply(e.getValue()));
        }

        return result;
    }

    //------------------ Delegate Methods -----------------//

    public void add(String property, JsonElement value) {
        json.add(property, value);
    }

    public JsonElement remove(String property) {
        return json.remove(property);
    }

    public void add(String property, String value) {
        json.addProperty(property, value);
    }

    public void add(String property, Number value) {
        json.addProperty(property, value);
    }

    public void add(String property, Boolean value) {
        json.addProperty(property, value);
    }

    public void add(String property, Character value) {
        json.addProperty(property, value);
    }

    public Set<Map.Entry<String, JsonElement>> entrySet() {
        return json.entrySet();
    }

    public int size() {
        return json.size();
    }

    public boolean has(String memberName) {
        return json.has(memberName);
    }

    public JsonElement get(String memberName) {
        return json.get(memberName);
    }

    public JsonPrimitive getAsJsonPrimitive(String memberName) {
        return json.getAsJsonPrimitive(memberName);
    }

    public JsonArray getAsJsonArray(String memberName) {
        return json.getAsJsonArray(memberName);
    }

    public JsonObject getAsJsonObject(String memberName) {
        return json.getAsJsonObject(memberName);
    }

    public JsonObject getSource() {
        return json;
    }

    public JsonObject nullIfEmpty(){
        return isEmpty() ? null : json;
    }

    public String toString(){
        return json.toString();
    }

    public boolean equals(Object o){
        return json.equals(o);
    }

    public int hashCode(){
        return json.hashCode();
    }
}
