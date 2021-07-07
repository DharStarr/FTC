package net.forthecrown.economy;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.chat.Announcer;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.serializer.AbstractYamlSerializer;
import net.forthecrown.user.UserManager;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class CrownBalances extends AbstractYamlSerializer implements Balances {

    private SortedBalanceMap balanceMap = new SortedBalanceMap(CrownCore::getStartRhines); //this is how all the balances are stored, in a private Map

    public CrownBalances() {
        super("balance");

        reload();
        CrownCore.logger().info("Balances loaded");
    }

    @Override
    public synchronized void saveFile(){
        Set<UUID> alreadySerialized = new HashSet<>();
        Set<UUID> toRemove = new HashSet<>();
        int defAmount = CrownCore.getStartRhines();

        for (BalanceMap.BalEntry e: balanceMap.entrySet()){
            if(alreadySerialized.contains(e.getUniqueId())){
                toRemove.add(e.getUniqueId());
                continue;
            }
            if(e.getValue() == defAmount) continue;

            alreadySerialized.add(e.getUniqueId());
            getFile().set(e.getUniqueId().toString(), e.getValue());
        }

        if(!toRemove.isEmpty()) toRemove.forEach(balanceMap::remove);
    }

    @Override
    public synchronized void reloadFile(){
        balanceMap.clear();

        for(String string : getFile().getKeys(true)){
            UUID id;
            try {
                id = UUID.fromString(string);
            } catch (Exception e){
                CrownCore.logger().warning(string + " is not a valid UUID, reload() in Balance");
                continue;
            }

            int balance = getFile().getInt(string);
            balanceMap.put(id, balance);
        }
    }

    @Override
    public synchronized BalanceMap getMap(){
        return balanceMap;
    }
    @Override
    public synchronized void setMap(SortedBalanceMap balanceMap){
        this.balanceMap = balanceMap;
    }

    @Override
    public synchronized Integer get(UUID uuid){
        return balanceMap.get(uuid);
    }

    @Override
    public synchronized void set(UUID uuid, Integer amount){
        if(amount >= CrownCore.getMaxMoneyAmount()) Announcer.log(Level.WARNING, Bukkit.getOfflinePlayer(uuid).getName() + " has reached the balance limit.");

        setUnlimited(uuid, Math.max(0, Math.min(CrownCore.getMaxMoneyAmount(), amount)));
    }

    @Override
    public synchronized void setUnlimited(UUID id, Integer amount){
        balanceMap.put(id, amount);
    }

    @Override
    public boolean canAfford(UUID id, int amount) {
        return get(id) >= amount;
    }

    @Override
    public synchronized void add(UUID uuid, Integer amount){
        add(uuid, amount, false);
    }

    @Override
    public synchronized void add(UUID uuid, Integer amount, boolean isTaxed){
        if(amount + get(uuid) >= CrownCore.getMaxMoneyAmount()){
            Announcer.log(Level.WARNING, Bukkit.getOfflinePlayer(uuid).getName() + " has reached the balance limit.");
            balanceMap.put(uuid, CrownCore.getMaxMoneyAmount());
            return;
        }

        if(amount > 0) UserManager.getUser(uuid).addTotalEarnings(amount);

        if(CrownCore.areTaxesEnabled() && isTaxed && getTax(uuid) > 1 && amount > 1){
            int amountToRemove = (int) (amount * ((float) getTax(uuid)/100));
            amount -= amountToRemove;

            UserManager.getUser(uuid).sendMessage("&7You were taxed " + getTax(uuid) + "%, which means you lost " + ChatFormatter.decimalizeNumber(amountToRemove) + " Rhines of your last transaction");
        }

        balanceMap.put(uuid, get(uuid) + amount);
    }

    @Override
    public Integer getTax(UUID uuid){
        if(get(uuid) < 500000) return 0; //if the player has less thank 500k rhines, no tax

        int percent = (int) (UserManager.getUser(uuid).getTotalEarnings() / 50000 * 10);
        if(percent >= 30) return 50;
        return percent;
    }
}
