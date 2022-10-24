package net.forthecrown.regions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Worlds;
import net.forthecrown.structure.*;
import net.forthecrown.utils.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3i;

import static net.forthecrown.regions.RegionManager.get;
import static net.kyori.adventure.text.Component.text;

public final class Regions {
    private Regions() {}

    /* ----------------------------- CONSTANTS ------------------------------ */

    /** The full width of a region */
    public static final int REGION_SIZE = 400;

    /** Half of a region's width */
    public static final int HALF_REGION_SIZE = REGION_SIZE / 2;

    /** The default name of the server's spawn region */
    public static final String DEFAULT_SPAWN_NAME = "Hazelguard";

    /** Name of the Region pole {@link net.forthecrown.structure.BlockStructure} */
    public static final String POLE_STRUCTURE = "region_pole";

    /** Default size of the pole (5, 5, 5) */
    public static Vector3i DEFAULT_POLE_SIZE = Vector3i.from(5);

    public static final String
            FUNC_REGION_NAME = "region_name",
            FUNC_RESIDENTS = "region_residents",
            FUNC_NEIGHBOR = "region_neighbour";

    /* ----------------------------- METHODS ------------------------------ */

    public static BlockStructure getRegionPole() {
        return Structures.get()
                .getRegistry()
                .orNull(POLE_STRUCTURE);
    }

    public static Vector3i poleSize() {
        return Structures.get()
                .getRegistry()
                .get(POLE_STRUCTURE)
                .map(BlockStructure::getDefaultSize)
                .orElse(DEFAULT_POLE_SIZE);
    }

    /**
     * Finds a region's pole bounding box.
     * <p>
     * Does this by calling {@link #bottomOfPole(World, Vector2i)} and
     * then calculating the result based off of hard coded pole size
     * values.
     *
     * @param region The region to find the bounding box for
     * @return The bounding box
     */
    public static Bounds3i poleBoundingBox(PopulationRegion region) {
        WorldVec3i p = bottomOfPole(getWorld(), region.getPolePosition());
        Vector3i size = poleSize();
        int halfX = size.x() / 2;
        int halfZ = size.z() / 2;

        return new Bounds3i(
                p.x() - halfX,
                p.y(),
                p.z() - halfZ,

                p.x() + halfX,
                p.y() + (size.y()),
                p.z() + halfZ
        );
    }

    /** Recursively move downward until pole bottom is reched */
    private static Vector3i findBottomLazy(Vector3i vec3i, World world) {
        Block mat = Vectors.getBlock(vec3i, world);

        if (mat.isEmpty()) {
            return findBottomLazy(vec3i.sub(0, 1, 0), world);
        }

        return switch (mat.getType()) {
            case OAK_SIGN -> vec3i.sub(0, 4, 0);
            case SEA_LANTERN -> vec3i.sub(0, 3, 0);
            case GLOWSTONE -> findBottomLazy(vec3i.sub(0, 1, 0), world);
            default -> vec3i;
        };
    }

    /**
     * Gets the bottom position of a pole.
     *
     * @param world The world the pole is in
     * @param vec2 The position of the pole
     * @return The pole's position
     */
    public static WorldVec3i bottomOfPole(World world, Vector2i vec2) {
        int y = world.getHighestBlockYAt(vec2.x(), vec2.y(), HeightMap.WORLD_SURFACE);
        Vector3i vec3i = Vector3i.from(vec2.x(), y, vec2.y());

        return new WorldVec3i(world, findBottomLazy(vec3i, world));
    }

    /**
     * Checks if the given 2D position is a valid place for a region post
     * @param region The region the pole is for
     * @param vec The place to put the pole
     * @return True, if the pole is not outside the region and is more than 5 blocks away from the edge
     */
    public static boolean isValidPolePosition(PopulationRegion region, Vector2i vec) {
        var size = poleSize();
        int sizeVal = Math.max(size.x(), size.z());

        var min = region.getPos().toAbsolute()
                .add(sizeVal, sizeVal);

        var max = min.add(REGION_SIZE, REGION_SIZE)
                .sub(sizeVal, sizeVal);

        // Ensure X in bounds
        return vec.x() >= min.x()
                && vec.x() <= max.x()

                // Ensure Y in bounds
                && vec.y() >= min.y()
                && vec.y() <= max.y();
    }

    /**
     * Validates that the given world is the region world
     * @param world The world to check
     * @throws CommandSyntaxException If the world is not the region world
     */
    public static void validateWorld(World world) throws CommandSyntaxException {
        if (!world.equals(getWorld())) {
            throw Exceptions.REGIONS_WRONG_WORLD;
        }
    }

    /**
     * Checks if the given user is in a valid distance to the pole
     * @param pole The pole's position
     * @param user The user
     *
     * @return True, if the user has the {@link Permissions#REGIONS_ADMIN} permission
     *         or if {@link #isCloseToPole(Vector2i, User)} returns true
     */
    public static boolean isCloseToPoleOrOp(Vector2i pole, User user) {
        return isCloseToPole(pole, user) || user.hasPermission(Permissions.REGIONS_ADMIN);
    }

    /**
     * Checks if the given user is within a valid distance to the
     * given pole position
     * <p>
     * Note: This distance check only occurs in the second dimension,
     * vertical height is not checked.
     *
     * @param pole The pole's position
     * @param user The user
     * @return True, if the user is close enough to the pole to use it
     */
    public static boolean isCloseToPole(Vector2i pole, User user) {
        var loc = user.getLocation();
        Vector2d userPos = Vector2d.from(loc.getX(), loc.getZ());
        Vector2d polePos = pole.toDouble().add(0.5D, 0.5D);

        double distance = userPos.distance(polePos);
        return distance <= (poleSize().length() / 2);
    }

    /**
     * Checks if the user has the required permission or is close enough
     * to the given pole
     *
     * @param pole The pole's position
     * @param user The user
     * @throws CommandSyntaxException If user either doesn't have the permission or isn't close enough
     */
    public static void validateDistance(Vector2i pole, User user) throws CommandSyntaxException {
        if (!isCloseToPoleOrOp(pole, user)) {
            throw Exceptions.farFromPole(pole.x(), pole.y());
        }
    }

    /**
     * Gets the world the region pole system operates in
     * @return The region world
     */
    public static World getWorld() {
        return Worlds.overworld();
    }

    /* ----------------------------- POLE GENERATION ------------------------------ */

    public static void placePole(PopulationRegion region) {
        var structure = Regions.getRegionPole();

        if (structure == null) {
            FTC.getLogger().warn("No pole structure found in registry! Cannot place!");
            return;
        }

        var config = StructurePlaceConfig.builder()
                .placeEntities(true)
                .addRotationProcessor()
                .addNonNullProcessor()
                .world(getWorld())

                .pos(region.getPoleBounds().min())

                // Function processors to ensure signs on pole
                // display correct information
                .addFunction(FUNC_REGION_NAME, (info, c) -> processTopSign(region, info, c))
                .addFunction(FUNC_NEIGHBOR,    (info, c) -> processNeighbourSign(region, info, c))
                .addFunction(FUNC_RESIDENTS,   (info, c) -> processResidentsSign(region, info, c))

                .build();

        structure.place(config);
    }

    private static void processTopSign(PopulationRegion region, FunctionInfo info, StructurePlaceConfig config) {
        var pos = config.getTransform().apply(info.getOffset());
        var world = config.getWorld();

        var block = Vectors.getBlock(pos, world);

        org.bukkit.block.data.type.Sign signData =
                (org.bukkit.block.data.type.Sign) Material.OAK_SIGN.createBlockData();

        signData.setRotation(BlockFace.NORTH);
        block.setBlockData(signData, false);

        Sign sign = (Sign) block.getState();

        sign.line(1, region.signName());
        sign.line(2, text("Region"));

        sign.update();
    }

    private static void processNeighbourSign(PopulationRegion region, FunctionInfo info, StructurePlaceConfig config) {
        var pos = config.getTransform().apply(info.getOffset());
        var world = config.getWorld();

        var block = Vectors.getBlock(pos, world);

        WallSign signData = (WallSign) Material.OAK_WALL_SIGN.createBlockData();

        signData.setFacing(info.getFacing().asBlockFace());

        block.setBlockData(signData);
        Sign sign = (Sign) block.getState();

        Component pointer = text("<----");
        var dir = info.getFacing().rotate(Rotation.CLOCKWISE_90);
        RegionAccess access = get().getAccess(region.getPos().add(dir.getMod().x(), dir.getMod().z()));

        sign.line(0, pointer);
        sign.line(1, access.signName());
        sign.line(2, Component.text("Region"));
        sign.line(3, pointer);

        sign.update();
    }

    private static void processResidentsSign(PopulationRegion region, FunctionInfo info, StructurePlaceConfig config) {
        if (region.hasProperty(RegionProperty.HIDE_RESIDENTS)
                || region.getResidency().isEmpty()
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
        var residents = region.getResidency();

        if (residents.size() == 1) {
            sign.line(1, text("Resident:"));
            sign.line(2,
                    Text.format("{0, user}",
                            residents.getEntries()
                                    .keySet()
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
}