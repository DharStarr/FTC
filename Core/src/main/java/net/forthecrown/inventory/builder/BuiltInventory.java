package net.forthecrown.inventory.builder;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.inventory.builder.options.InventoryRunnable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class BuiltInventory implements InventoryHolder {

    private final Int2ObjectMap<InventoryOption> options;
    private final Component title;
    private final int size;
    private final boolean freeInventory;

    private final InventoryCloseAction onClose;
    private final InventoryAction onOpen;
    private final InventoryRunnable onClick;

    public BuiltInventory(Int2ObjectMap<InventoryOption> options,
                          Component title,
                          int size,
                          boolean inventory, InventoryCloseAction onClose,
                          InventoryAction onOpen,
                          InventoryRunnable click) {
        this.options = new Int2ObjectOpenHashMap<>(options);
        this.title = title;
        this.size = size;
        freeInventory = inventory;
        this.onClose = onClose;
        this.onOpen = onOpen;
        onClick = click;
    }

    @Override
    public @NotNull Inventory getInventory() {
        throw new UnsupportedOperationException("Use open(CrownUser)");
    }

    public boolean hasOption(int slot){
        return getOptions().containsKey(slot);
    }

    public InventoryOption getOption(int slot){
        return getOptions().get(slot);
    }

    public void run(Player player, InventoryClickEvent event){
        ClickContext context = new ClickContext(
                (FtcInventory) event.getClickedInventory(), player,
                event.getSlot(), event.getCursor(), event.getClick()
        );

        try {
            CrownUser user = UserManager.getUser(player);

            if(onClick != null) onClick.onClick(user, context);
            if(Cooldown.contains(player, getClass().getSimpleName())) return;

            InventoryOption option = getOptions().get(event.getSlot());
            if(option == null) return;

            option.onClick(user, context);

            if(context.shouldCooldown()) Cooldown.add(player, getClass().getSimpleName(), 5);
            if(context.shouldReload()) open(player);

            event.setCancelled(context.shouldCancelEvent());

            if(context.shouldClose()) event.getWhoClicked().closeInventory();
        } catch (CommandSyntaxException e) {
            FtcUtils.handleSyntaxException(player, e);
        }
    }

    public void open(Player player) {
        open(UserManager.getUser(player));
    }

    public void open(CrownUser user){
        if(getOnOpen() != null) getOnOpen().run(user);

        Inventory inventory = createInventory(user);
        user.getPlayer().openInventory(inventory);
    }

    public FtcInventory createInventory(CrownUser user){
        FtcInventory inv = FtcInventory.of(this, size, title);

        ObjectList<InventoryOption> lows = new ObjectArrayList<>();
        ObjectList<InventoryOption> mids = new ObjectArrayList<>();
        ObjectList<InventoryOption> highs = new ObjectArrayList<>();

        for (InventoryOption o: options.values()){
            o.getPriority().pick(lows, mids, highs).add(o);
        }

        lows.forEach(o -> o.place(inv, user));
        mids.forEach(o -> o.place(inv, user));
        highs.forEach(o -> o.place(inv, user));

        return inv;
    }

    public InventoryCloseAction getOnClose() {
        return onClose;
    }

    public InventoryAction getOnOpen() {
        return onOpen;
    }

    public Int2ObjectMap<InventoryOption> getOptions() {
        return Int2ObjectMaps.unmodifiable(options);
    }

    public boolean isFreeInventory() {
        return freeInventory;
    }

    public InventoryRunnable getOnClick() {
        return onClick;
    }
}
