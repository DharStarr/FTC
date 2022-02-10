package net.forthecrown.core;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.Bukkit2NMS;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.utils.world.WorldLoader;
import net.forthecrown.utils.world.WorldReCreator;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.apache.commons.lang3.Range;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The end opener should automatically close and open the end portal in
 * Hazelguard.
 * It should be kept open for the last 7 days of each week, aka, end week.
 */
public class EndOpener extends FtcConfig.ConfigSection implements DayChangeListener {
    private Component openMessage, closeMessage;
    private WorldVec3i leverPos;
    private boolean open;
    private boolean enabled;
    private int endSize;

    public EndOpener() {
        super("end_opener");

        Crown.getDayChange().addListener(this);
    }

    @Override
    public void onDayChange() {
        // If we've disabled this, don't run
        if(!enabled) return;

        byte day = (byte) Calendar.getInstance().get(Calendar.DATE);
        byte first = 1;
        byte last = (byte) YearMonth.now().lengthOfMonth();
        byte openingDay = (byte) (last - 7);

        // The range representing days on which the
        // portal should be closed
        Range<Byte> closedRange = Range.between(first, openingDay);

        // idk, if it's the middle of the month, reset the end
        // Preferably perform the resetting asynchronously.
        int rangeDif = closedRange.getMaximum() - closedRange.getMinimum();
        int resetDay = closedRange.getMinimum() + (rangeDif / 2);
        if(day == resetDay) regen();

        // If current day is a closed day, but portal not closed,
        // close it, if not close day and portal not open, open it
        // This is messy, but IDK how to do it better
        if(closedRange.contains(day)) {
            if(open) setOpen(false);
        } else if(!open) setOpen(true);
    }

    /**
     * Regenerates the end with a new seed and {@link EndOpener#getEndSize()} size.
     */
    public CompletableFuture<World> regen() {
        // Kick any players out of the end
        kickPlayers();

        // Re-create world
        WorldReCreator reCreator = WorldReCreator.of(Worlds.end())
                .preserveGameRules(true)
                .preserveSeed(false)
                .preserveWorldBorder(true);

        final World world = reCreator.run();

        // Sometimes the border size is incorrect
        // so gotta call this with our manually
        // set size
        world.getWorldBorder().setSize(endSize);

        // Load the world and create the crucial
        // End Features needed for the end to
        // function properly
        return WorldLoader.loadAsync(world)
                .whenComplete((world1, throwable) -> {
                    // Run sync
                    Bukkit.getScheduler().runTask(Crown.inst(), () -> {
                        try {
                            EndDragonFight fight = Bukkit2NMS.getLevel(world).dragonFight();

                            // Create exit portal
                            world.getEnderDragonBattle().generateEndPortal(true);

                            // Place gateways
                            new EndGateWayPlacer().place(fight);
                        } catch (ReflectiveOperationException e) {
                            e.printStackTrace();
                        }

                        Crown.logger().info("Placed end exit portal and gateways");
                    });
                });
    }

    private void kickPlayers() {
        Location exitLocation = FtcUtils.findHazelLocation();

        for (Player p: Worlds.end().getPlayers()) {
            p.teleport(exitLocation);
        }
    }

    /**
     * Opens or closes the end
     * @param open Whether the end is to be open
     */
    public void setOpen(boolean open) {
        // If lever setting fails, don't proceed
        if(!setLever(open)) return;

        this.open = open;
        PortalRoad.set(leverPos.getWorld(), open);

        Crown.getJoinInfo().setShouldShowEnd(open);

        Component message = open ? openMessage : closeMessage;
        Crown.getAnnouncer().announce(message);
    }

    // Lever on = closed, lever off = open. AKA, flip the input
    // Returns: True, if successfully changed lever, false otherwise
    private boolean setLever(boolean open) {
        boolean on = !open;

        Block b = leverPos.getBlock();

        if(b.getType() != Material.LEVER) {
            Crown.logger().error("Given EndOpener lever position: {} is not a lever! Cannot close/open end", leverPos);
            return false;
        }

        Switch data = (Switch) b.getBlockData();
        data.setPowered(on);

        b.setBlockData(data, true);

        // Update redstone
        // Thank you once more, Bukkit, for not providing
        // an API for this stuff. Cuz who could ever want
        // to update a block state
        Level world = Bukkit2NMS.getLevel(b.getWorld());
        CraftBlock craft = (CraftBlock) b;
        BlockPos pos = craft.getPosition();
        BlockState state = craft.getNMS();

        world.updateNeighborsAt(pos, Blocks.LEVER);
        world.updateNeighborsAt(pos.relative(getConnectedDirection(state).getOpposite()), Blocks.LEVER);
        return true;
    }

    // Copy and pasted method from LeverBlock class, it was protected there
    // So I had to lol
    protected static Direction getConnectedDirection(BlockState state) {
        return switch (state.getValue(LeverBlock.FACE)) {
            case CEILING -> Direction.DOWN;
            case FLOOR -> Direction.UP;
            default -> state.getValue(LeverBlock.FACING);
        };
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isOpen() {
        return open;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Component getCloseMessage() {
        return closeMessage;
    }

    public void setCloseMessage(Component closeMessage) {
        this.closeMessage = closeMessage;
    }

    public Component getOpenMessage() {
        return openMessage;
    }

    public void setOpenMessage(Component openMessage) {
        this.openMessage = openMessage;
    }

    public void setLeverPos(WorldVec3i leverPos) {
        this.leverPos = leverPos;
    }

    public WorldVec3i getLeverPos() {
        return leverPos;
    }

    public int getEndSize() {
        return endSize;
    }

    public void setEndSize(int endSize) {
        this.endSize = endSize;

        if(isOpen()) {
            Worlds.end().getWorldBorder().setSize(endSize);
        }
    }

    @Override
    public void deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        this.enabled = json.getBool("enabled");
        this.open = json.getBool("open");
        this.endSize = json.getInt("size");

        this.leverPos = WorldVec3i.of(json.get("lever"));
        this.closeMessage = json.getComponent("close_message");
        this.openMessage = json.getComponent("open_message");
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("enabled", enabled);
        json.add("open", open);
        json.add("size", endSize);

        json.add("lever", leverPos);
        json.addComponent("close_message", closeMessage);
        json.addComponent("open_message", openMessage);

        return json.getSource();
    }

    static class PortalRoad {
        static final Material[] ROAD_MATERIALS = {
                Material.COBBLESTONE,
                Material.GRAVEL,
                Material.STONE_BRICKS,
                Material.CRACKED_STONE_BRICKS
        };

        // Hardcoded road entry values, cuz I can't be arsed
        // doing this in other, more dynamic, ways
        public static final PortalRoadEntry[] ROAD_ENTRIES = {
                //castle courtyard
                new PortalRoadEntry(240, 77, 198),
                new PortalRoadEntry(239, 77, 198),

                new PortalRoadEntry(234, 77, 200),
                new PortalRoadEntry(233, 77, 200),
                new PortalRoadEntry(232, 77, 200),

                new PortalRoadEntry(227, 77, 200),
                new PortalRoadEntry(226, 77, 200),

                new PortalRoadEntry(221, 77, 200),

                //near pole
                new PortalRoadEntry(207, 69, 200, Material.POLISHED_ANDESITE),
                new PortalRoadEntry(206, 69, 200, Material.POLISHED_ANDESITE),

                //stairs
                new PortalRoadEntry(217, 75, 200, Material.POLISHED_ANDESITE),
                new PortalRoadEntry(212, 71, 200, Material.POLISHED_ANDESITE),
                new PortalRoadEntry(221, 77, 200, Material.POLISHED_ANDESITE)
        };

        public static void set(World world, boolean glass) {
            for (PortalRoadEntry e: ROAD_ENTRIES) {
                Block b = world.getBlockAt(e.x, e.y, e.z);

                b.setType(glass ? Material.PURPLE_STAINED_GLASS : e.material());
            }
        }

        record PortalRoadEntry(int x, int y, int z, Material material) {
            PortalRoadEntry(int x, int y, int z) {
                this(x, y, z, null);
            }

            @Override
            public Material material() {
                return material == null ? ROAD_MATERIALS[FtcUtils.RANDOM.nextInt(ROAD_MATERIALS.length)] : material;
            }
        }
    }

    private static class EndGateWayPlacer {
        private final Method newGateway;
        private final Field possibleGateways;

        private EndGateWayPlacer() throws ReflectiveOperationException {
            newGateway = EndDragonFight.class.getDeclaredMethod("n");
            newGateway.setAccessible(true);

            possibleGateways = EndDragonFight.class.getDeclaredField("m");
            possibleGateways.setAccessible(true);
        }

        private void place(EndDragonFight fight) throws ReflectiveOperationException {
            List<Integer> possible = (List<Integer>) possibleGateways.get(fight);
            int length = possible.size();

            for (int i = 0; i < length; i++) {
                newGateway.invoke(fight);
            }
        }
    }
}