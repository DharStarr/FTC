package net.forthecrown.commands.test;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.FTC;
import net.forthecrown.dungeons.DungeonWorld;
import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.dungeons.level.DungeonRoom;
import net.forthecrown.dungeons.level.PieceVisitor;
import net.forthecrown.dungeons.level.gate.AbsoluteGateData;
import net.forthecrown.dungeons.level.gate.DungeonGate;
import net.forthecrown.dungeons.level.generator.TreeGenerator;
import net.forthecrown.dungeons.level.generator.TreeGeneratorConfig;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.Particles;
import net.forthecrown.utils.Tasks;
import org.apache.logging.log4j.Logger;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

public class CommandTestDungeon extends FtcCommand {

    public static final int RENDER_INTERVAL = 15;
    public static final double DIST = 0.5D;
    public static final float SIZE = 3F;
    public static final boolean DRAW_GATES = false;

    private static final Logger LOGGER = FTC.getLogger();

    public CommandTestDungeon() {
        super("TestDungeon");

        initGenerator();
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /TestDungeon
     *
     * Permissions used:
     *
     * Main Author:
     */

    private TreeGenerator generator;
    private BukkitTask renderer;

    private void toggleRender() {
        if (renderer == null) {
            Renderer renderer = new Renderer();
            this.renderer = Tasks.runTimer(renderer, RENDER_INTERVAL, RENDER_INTERVAL);
        } else {
            renderer = Tasks.cancel(renderer);
        }
    }

    private void initGenerator() {
        this.generator = new TreeGenerator(TreeGeneratorConfig.defaultConfig());
        generator.reset();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("make")
                        .executes(c -> place(c.getSource()))
                )

                .then(literal("reset_world")
                        .executes(c -> {
                            c.getSource().sendMessage("Resetting dungeon world");

                            DungeonWorld.reset();
                            return 0;
                        })
                );
    }

    private int place(CommandSource source) {
        DungeonWorld.reset();

        var config = TreeGeneratorConfig.defaultConfig();
        TreeGenerator.generateAsync(config)
                .whenComplete((level, throwable) -> {
                    if (throwable != null) {
                        LOGGER.error("Couldn't generate level!", throwable);
                        source.sendMessage("Couldn't generate level!");

                        return;
                    }

                    Tasks.runSync(() -> {
                        source.sendAdmin("Placing dungeon level");
                        level.place();
                    });
                });

        return 0;
    }

    private class Renderer implements Runnable {
        private static final Vector3d HALF = Vector3d.from(0.5D);

        private static final Color
                ENTRANCE_OPENING = Color.GREEN,
                EXIT_OPENING = Color.YELLOW,
                GATE = Color.BLUE,
                GATE_MIN = Color.AQUA,
                ROOM = Color.RED,
                ROOM_MIN = Color.PURPLE;

        private Color color;

        private final PieceVisitor walker = new PieceVisitor() {
            @Override
            public Result onGate(DungeonGate gate) {
                render(gate, GATE, GATE_MIN);

                if (gate.getTargetGate() != null) {
                    drawGate(gate.getTargetGate(), EXIT_OPENING);
                }

                if (gate.getOriginGate() != null) {
                    drawGate(gate.getOriginGate(), ENTRANCE_OPENING);
                }

                return Result.CONTINUE;
            }

            @Override
            public Result onRoom(DungeonRoom room) {
                render(room, ROOM, ROOM_MIN);
                return Result.CONTINUE;
            }
        };

        @Override
        public void run() {
            var level = generator.getLevel();

            if (level == null || level.getRoot() == null) {
                return;
            }

            level.getRoot().visit(walker);
        }

        private void render(DungeonPiece piece, Color color, Color colorMin) {
            var bb = piece.getBounds();
            var min = bb.min();
            var max = bb.max();

            Vector3i[] corners = {
                    min,
                    min.withX(max.x()),
                    max.withY(min.y()),
                    min.withZ(max.z()),

                    max,
                    max.withX(min.x()),
                    min.withY(max.y()),
                    max.withZ(min.z())
            };

            begin(color);
                // Bottom
                line(corners[0], corners[1]);
                line(corners[1], corners[2]);
                line(corners[2], corners[3]);
                line(corners[3], corners[0]);

                // Top
                line(corners[4], corners[5]);
                line(corners[5], corners[6]);
                line(corners[6], corners[7]);
                line(corners[7], corners[4]);

                // Sides
                line(corners[1], corners[7]);
                line(corners[5], corners[3]);
                line(corners[4], corners[2]);

            // Min corner
            begin(colorMin);
                line(corners[0], corners[6]);
        }

        private void drawGate(AbsoluteGateData data, Color color) {
            if (!DRAW_GATES) {
                return;
            }

            var left = data.direction().left();
            var open = data.opening();

            Vector3i min = data.rightSide();
            Vector3i max = min.add(left.getMod().mul(open.width(), 0, open.width()))
                    .add(0, open.height(), 0);

            min = min.add(left.getMod()).add(0, 1, 0);
            max = max.sub(left.getMod()).sub(0, 1, 0);

            Vector3i[] corners = {
                    min,
                    max.withY(min.y()),

                    max,
                    min.withY(max.y())
            };

            begin(color);
                line(corners[0], corners[1]);
                line(corners[1], corners[2]);
                line(corners[2], corners[3]);
                line(corners[3], corners[0]);

                line(corners[0], corners[2]);
                line(corners[1], corners[3]);
        }

        private void begin(Color color) {
            this.color = color;
        }

        private void line(Vector3i start, Vector3i end) {
            Particles.line(
                    start.toDouble().add(HALF),
                    end.toDouble().add(HALF),
                    DIST,

                    DungeonWorld.get(),

                    Particle.REDSTONE.builder()
                            .data(new Particle.DustOptions(color, SIZE))
            );
        }
    }
}