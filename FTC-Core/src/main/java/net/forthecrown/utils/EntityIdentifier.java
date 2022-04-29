package net.forthecrown.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Data;
import net.forthecrown.grenadier.types.UUIDArgument;
import net.minecraft.world.level.ChunkPos;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.UUID;

@Data(staticConstructor = "of")
public class EntityIdentifier {
    public static final String FIELD_SEPARATOR = " ";

    private final UUID uniqueId;
    private final String worldName;
    private final ChunkPos chunk;

    public World getWorld() {
        return Bukkit.getWorld(getWorldName());
    }

    public Entity get() {
        World w = getWorld();
        if (w == null) return null;

        Chunk c = w.getChunkAt(chunk.x, chunk.z);

        for (Entity e: c.getEntities()) {
            if(e.getUniqueId().equals(uniqueId)) {
                return e;
            }
        }

        return null;
    }

    public static EntityIdentifier of(Entity e) {
        Location l = e.getLocation();
        ChunkPos chunkPos = new ChunkPos(l.getBlockX() >> 4, l.getBlockZ() >> 4);

        return new EntityIdentifier(e.getUniqueId(), l.getWorld().getName(), chunkPos);
    }

    public static EntityIdentifier parse(String input) {
        try {
            StringReader reader = new StringReader(input);
            return parse(reader);
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static EntityIdentifier parse(StringReader reader) throws CommandSyntaxException {
        String worldName = reader.readString();
        reader.expect(' ');

        int chunkX = reader.readInt();
        reader.expect(' ');

        int chunkZ = reader.readInt();

        UUID id = UUIDArgument.uuid().parse(reader);

        return new EntityIdentifier(id, worldName, new ChunkPos(chunkX, chunkZ));
    }

    @Override
    public String toString() {
        return worldName + FIELD_SEPARATOR + chunk.x + FIELD_SEPARATOR + chunk.z + FIELD_SEPARATOR + uniqueId;
    }
}