package net.forthecrown.economy.pirates.merchants;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.comvars.ComVar;
import net.forthecrown.comvars.ComVarRegistry;
import net.forthecrown.comvars.types.ComVarType;
import net.forthecrown.economy.Balances;
import net.forthecrown.squire.Squire;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.*;
import net.forthecrown.utils.math.BlockPos;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class HeadMerchant implements BlackMarketMerchant {
    public static final Key KEY = Squire.createPiratesKey("heads");

    private BlockPos[] chests;
    private PlayerProfile profile;
    private final List<String> alreadyChosen = new ArrayList<>();
    private final Set<UUID> alreadySold = new HashSet<>();

    private ComVar<Integer> moneyReward;
    private ComVar<Byte> piratePointReward;

    public HeadMerchant() {
        this.chests = new BlockPos[4];
    }

    @Override
    public Inventory createInventory(CrownUser user) {
        throw new UnsupportedOperationException("createInventory");
    }

    @Override
    public void load(JsonElement element) {
        JsonObject json = element.getAsJsonObject();

        piratePointReward = ComVarRegistry.set("pr_heads_ppReward", ComVarType.BYTE, json.get("ppReward").getAsByte());
        moneyReward = ComVarRegistry.set("pr_heads_moneyReward", ComVarType.INTEGER, json.get("moneyReward").getAsInt());

        JsonArray array = json.getAsJsonArray("chests");
        chests = new BlockPos[array.size()];
        int index = 0;

        for (JsonElement e: array){
            chests[index] = BlockPos.of(e);
            index++;
        }

        alreadyChosen.clear();
        if(json.has("alreadyChosen")){
            JsonArray pickedArray = json.getAsJsonArray("alreadyChosen");
            pickedArray.forEach(e -> alreadyChosen.add(e.getAsString()));
        }

        alreadySold.clear();
        if(json.has("alreadySold")){
            JsonArray soldArray = json.getAsJsonArray("alreadySold");
            soldArray.forEach(e -> alreadySold.add(UUID.fromString(e.getAsString())));
        }

        try {
            this.profile = new CraftPlayerProfile(NbtUtils.readGameProfile(TagParser.parseTag(json.get("head").getAsString())));
        } catch (CommandSyntaxException e){
            e.printStackTrace();
        }
    }

    @Override
    public void update(CrownRandom random, byte day) {
        BlockPos pos = chests[random.intInRange(0, chests.length)];
        Inventory inv = ((Chest) pos.getBlock(Worlds.NORMAL)).getBlockInventory();
        alreadySold.clear();

        ItemStack item;
        boolean validPick = false;

        while(!validPick){
            item = inv.getItem(random.intInRange(0, inv.getSize()));
            if(item == null) continue;

            SkullMeta meta = (SkullMeta) item.getItemMeta();
            PlayerProfile profile = meta.getPlayerProfile();

            if(alreadyChosen.contains(profile.getName())) continue;

            this.profile = profile;
            validPick = true;
        }

        if(day == 0 || day == 1) alreadyChosen.clear();
        alreadyChosen.add(profile.getName());
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        JsonArray array = new JsonArray();
        for (BlockPos p: chests){
            array.add(p.serialize());
        }

        json.add("head", new JsonPrimitive(NbtUtils.writeGameProfile(new CompoundTag(), ((CraftPlayerProfile) profile).getGameProfile()).toString()));
        json.add("chests", array);

        json.addProperty("ppReward", getPiratePointReward());
        json.addProperty("moneyReward", getMoneyReward());

        if(!alreadyChosen.isEmpty()) json.add("alreadyChosen", JsonUtils.writeCollection(alreadyChosen, JsonPrimitive::new));
        if(!alreadySold.isEmpty()) json.add("alreadySold", JsonUtils.writeCollection(alreadySold, id -> new JsonPrimitive(id.toString())));

        return json;
    }

    public Component headDisplayName(){
        return Component.text(profile.getName());
    }

    @Override
    public void onUse(CrownUser user, Entity entity) {
        if(alreadySold.contains(user.getUniqueId())){
            user.sendMessage(Component.translatable("pirates.heads.alreadySold", headDisplayName()));
            return;
        }

        PlayerInventory inv = user.getPlayer().getInventory();

        ItemStack item = null;
        for (ItemStack i: inv){
            if(CrownUtils.isItemEmpty(i)) continue;
            if(i.getType() != Material.PLAYER_HEAD) continue;

            SkullMeta meta = (SkullMeta) i.getItemMeta();
            if(!meta.getPlayerProfile().equals(profile)) continue;

            item = i;
        }

        if(item == null){
            user.sendMessage(Component.translatable("pirates.heads.none", NamedTextColor.RED, headDisplayName()));
            return;
        }

        item.subtract();
        Score pp = BlackMarketUtils.getPiratePointScore(user.getName());
        pp.setScore(pp.getScore() + getPiratePointReward());

        user.sendMessage(
                Component.text()
                        .color(NamedTextColor.YELLOW)
                        .append(Component.translatable("pirates.heads.sold", headDisplayName().color(NamedTextColor.YELLOW)))
                        .append(Component.newline())
                        .append(Component.translatable(
                                "pirates.heads.reward",
                                Balances.formatted(getMoneyReward()).color(NamedTextColor.YELLOW),
                                Component.text(getPiratePointReward() + " Pirate Point" + CrownUtils.addAnS(getPiratePointReward())).color(NamedTextColor.YELLOW),
                                Component.text(pp.getScore()).color(NamedTextColor.YELLOW)
                        ))
        );
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public BlockPos[] getChests() {
        return chests;
    }

    public List<String> getAlreadyChosen() {
        return alreadyChosen;
    }

    public PlayerProfile getProfile() {
        return profile;
    }

    public int getMoneyReward(){
        return moneyReward.getValue(10000);
    }

    public byte getPiratePointReward(){
        return piratePointReward.getValue((byte) 10);
    }
}
