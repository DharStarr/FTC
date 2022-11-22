package net.forthecrown.waypoint;

import com.google.common.base.Strings;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.experimental.UtilityClass;
import net.forthecrown.commands.arguments.WaypointArgument;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.FTC;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.FunctionInfo;
import net.forthecrown.structure.StructurePlaceConfig;
import net.forthecrown.structure.Structures;
import net.forthecrown.user.User;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.text.Text;
import net.forthecrown.waypoint.type.PlayerWaypointType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static net.kyori.adventure.text.Component.text;

public @UtilityClass class Waypoints {
    /* ------------------------- COLUMN CONSTANTS --------------------------- */

    /** The required center column for guild waypoints */
    public final Material[] GUILD_COLUMN = {
            Material.STONE_BRICKS,
            Material.STONE_BRICKS,
            Material.LODESTONE,
    };

    /** The required center column for player waypoints */
    public final Material[] PLAYER_COLUMN = {
            Material.STONE_BRICKS,
            Material.STONE_BRICKS,
            Material.CHISELED_STONE_BRICKS,
    };

    /** The required center column for region poles */
    public final Material[] REGION_POLE_COLUMN = {
            Material.GLOWSTONE,
            Material.GLOWSTONE,
            Material.SEA_LANTERN
    };

    /** Name of the Region pole {@link net.forthecrown.structure.BlockStructure} */
    public final String POLE_STRUCTURE = "region_pole";

    public final String FUNC_REGION_NAME = "region_name";

    public final String FUNC_RESIDENTS = "region_residents";

    /** Default size of the pole (5, 5, 5) */
    public Vector3i DEFAULT_POLE_SIZE = Vector3i.from(5);

    public BlockStructure getRegionPole() {
        return Structures.get()
                .getRegistry()
                .orNull(POLE_STRUCTURE);
    }

    public Vector3i poleSize() {
        return Structures.get()
                .getRegistry()
                .get(POLE_STRUCTURE)
                .map(BlockStructure::getDefaultSize)
                .orElse(DEFAULT_POLE_SIZE);
    }

    public void placePole(Waypoint region) {
        var structure = getRegionPole();

        if (structure == null) {
            FTC.getLogger().warn("No pole structure found in registry! Cannot place!");
            return;
        }

        var config = StructurePlaceConfig.builder()
                .placeEntities(true)
                .addNonNullProcessor()
                .addRotationProcessor()
                .world(region.getWorld())

                .pos(region.getBounds().min())

                // Function processors to ensure signs on pole
                // display correct information
                .addFunction(
                        FUNC_REGION_NAME,
                        (info, c) -> processTopSign(region, info, c)
                )
                .addFunction(
                        FUNC_RESIDENTS,
                        (info, c) -> processResidentsSign(region, info, c)
                )

                .build();

        structure.place(config);
    }

    private void processTopSign(Waypoint region,
                                FunctionInfo info,
                                StructurePlaceConfig config
    ) {
        var pos = config.getTransform().apply(info.getOffset());
        var world = config.getWorld();

        var block = Vectors.getBlock(pos, world);

        org.bukkit.block.data.type.Sign signData =
                (org.bukkit.block.data.type.Sign)
                        Material.OAK_SIGN.createBlockData();

        signData.setRotation(BlockFace.NORTH);
        block.setBlockData(signData, false);

        Sign sign = (Sign) block.getState();

        sign.line(1, signName(region));
        sign.line(2, text("Waypoint"));

        sign.update();
    }

    private static void processResidentsSign(Waypoint region,
                                             FunctionInfo info,
                                             StructurePlaceConfig config
    ) {
        if (region.get(WaypointProperties.HIDE_RESIDENTS)
                || region.getResidents().isEmpty()
        ) {
            return;
        }

        var pos = config.getTransform().apply(info.getOffset());
        var world = config.getWorld();
        var block = Vectors.getBlock(pos, world);

        WallSign signData = (WallSign) Material.OAK_WALL_SIGN.createBlockData();
        signData.setFacing(info.getFacing().asBlockFace());
        block.setBlockData(signData);

        Sign sign = (Sign) block.getState();
        var residents = region.getResidents();

        if (residents.size() == 1) {
            sign.line(1, text("Resident:"));
            sign.line(2,
                    Text.format("{0, user}",
                            residents.keySet()
                                    .iterator()
                                    .next()
                    )
            );
        } else {
            sign.line(1, text("Residents:"));
            sign.line(2, text(residents.size()));
        }

        sign.update();
    }

    private Component signName(Waypoint waypoint) {
        var name = waypoint.get(WaypointProperties.NAME);
        return text(Strings.isNullOrEmpty(name) ? "Wilderness" : name);
    }

    public Set<Waypoint> getInvulnerable(Bounds3i bounds3i, World world) {
        return filterSet(
                WaypointManager.getInstance()
                        .getChunkMap()
                        .getOverlapping(world, bounds3i)
        );
    }

    public Set<Waypoint> getInvulnerable(Vector3i pos, World world) {
        return filterSet(
                WaypointManager.getInstance()
                        .getChunkMap()
                        .get(world, pos)
        );
    }

    private Set<Waypoint> filterSet(Set<Waypoint> waypoints) {
        waypoints.removeIf(waypoint -> !waypoint.get(WaypointProperties.INVULNERABLE));
        return waypoints;
    }

    public Waypoint getColliding(Player player) {
        return WaypointManager.getInstance()
                .getChunkMap()
                .getOverlapping(
                        player.getWorld(),
                        Bounds3i.of(player.getBoundingBox())
                )
                .stream()
                .findAny()
                .orElse(null);
    }

    public Waypoint getNearest(User user) {
        return WaypointManager.getInstance()
                .getChunkMap()
                .findNearest(user.getLocation())
                .left();
    }

    public boolean isValidName(String name) {
        return !BannedWords.contains(name)
                && !name.contains(" ")
                && !name.equalsIgnoreCase(WaypointArgument.FLAG_NEAREST)
                && !name.equalsIgnoreCase(WaypointArgument.FLAG_CURRENT);
    }

    public Optional<CommandSyntaxException> isValidWaypointArea(Vector3i pos,
                                                                PlayerWaypointType type,
                                                                World w,
                                                                boolean testOverlap
    ) {
        var bounds = type.createBounds()
                .move(pos)
                .expand(0, 1, 0, 0, 0, 0)
                .toWorldBounds(w);

        Material[] column = type.getColumn();

        // Test to make sure the area is empty and
        // contains the given type's column
        for (var b: bounds) {
            // If currently in column position
            if (b.getX() == pos.x() && b.getZ() == pos.z()) {
                int offset = b.getY() - pos.y();

                // Within column bounds
                if (offset < column.length
                        && offset >= 0
                ) {
                    Material required = column[offset];

                    // If the column block is not the block
                    // that is required to be here, then
                    // return exception, else, skip this block
                    if (b.getType() == required) {
                        continue;
                    }

                    return Optional.of(
                            Exceptions.brokenWaypoint(
                                    pos.add(0, offset, 0),
                                    b.getType(),
                                    required
                            )
                    );
                }
            }

            // If we're on the minY level, which would be the
            // layer right under the waypoint, return an exception,
            // since this layer must be solid, if it is solid,
            // skip block
            if (bounds.minY() == b.getY()) {
                if (b.isSolid()) {
                    continue;
                }

                return Optional.of(
                        Exceptions.waypointPlatform()
                );
            }

            // Test if block is empty
            if (b.isEmpty() || !b.isCollidable() || b.isPassable()) {
                continue;
            }

            return Optional.of(
                    Exceptions.waypointBlockNotEmpty(b)
            );
        }

        if (testOverlap) {
            Set<Waypoint> overlapping = WaypointManager.getInstance()
                    .getChunkMap()
                    .getOverlapping(bounds);

            if (!overlapping.isEmpty()) {
                return Optional.of(
                        Exceptions.overlappingWaypoints(overlapping.size())
                );
            }
        }

        return Optional.empty();
    }

    public void setNameSign(Waypoint waypoint, String name) {
        if (!(waypoint.getType() instanceof PlayerWaypointType type)) {
            throw new IllegalStateException(
                    "Only player/guild waypoints can have manual name signs"
            );
        }

        Vector3i pos = waypoint.getPosition()
                .add(0, type.getColumn().length - 1, 0);

        World w = waypoint.getWorld();
        Objects.requireNonNull(w, "World unloaded");

        Block b = Vectors.getBlock(pos, w);

        if (Strings.isNullOrEmpty(name)) {
            b.setType(Material.AIR);
        } else {
            b.setBlockData(Material.OAK_SIGN.createBlockData());

            Sign sign = (Sign) b.getState();
            sign.line(1, text(name));
            sign.line(2, text("Waypoint"));
            sign.update();
        }
    }
}