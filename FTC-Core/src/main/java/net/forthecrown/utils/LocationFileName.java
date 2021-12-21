package net.forthecrown.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.utils.math.WorldVec3i;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public record LocationFileName(String world,
                               int x, int y, int z)
{

    public static LocationFileName parse(String fName) {
        try {
            // lastIndex meaning the index before the file type, so we don't read the .json at the end
            // or something like that
            int lastIndex = fName.lastIndexOf('.');
            if (lastIndex == -1) lastIndex = fName.length();

            StringReader reader = new StringReader(fName.substring(0, lastIndex));

            //Read the world name at the start
            while (reader.canRead() && !StringReader.isAllowedNumber(reader.peek())) {
                reader.skip();
            }

            // Get the world name, - 1 from the length since it ends with the "_" before the integer
            String read = reader.getRead();
            String world = read.substring(0, read.length() - 1);

            //Read cords
            int x = reader.readInt();
            reader.skip();
            int y = reader.readInt();
            reader.skip();
            int z = reader.readInt();

            return new LocationFileName(world, x, y, z);
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public static LocationFileName of(Location l) {
        return new LocationFileName(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    public static LocationFileName of(WorldVec3i v) {
        return new LocationFileName(v.getWorld().getName(), v.getX(), v.getY(), v.getZ());
    }

    public Location toLocation() {
        return new Location(getWorld(), x, y, z);
    }

    public WorldVec3i toVector() {
        return new WorldVec3i(getWorld(), x, y, z);
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    @Override
    public String toString() {
        return world + '_' + x + '_' + y + '_' + z;
    }

    public String toString(String suffix) {
        return this + suffix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        LocationFileName name = (LocationFileName) o;

        return new EqualsBuilder()
                .append(x, name.x)
                .append(y, name.y)
                .append(z, name.z)
                .append(world, name.world)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(world)
                .append(x)
                .append(y)
                .append(z)
                .toHashCode();
    }
}