package net.forthecrown.regions;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.FtcDynmap;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldBounds3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Material;
import org.dynmap.markers.Marker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3i;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class PopulationRegion implements RegionAccess {
    @Getter
    private final RegionPos pos;

    private final EnumSet<RegionProperty> properties = EnumSet.noneOf(RegionProperty.class);

    @Getter @Setter(AccessLevel.PACKAGE)
    private String name;

    private Vector2i polePosition;

    @Getter
    private Bounds3i poleBounds;

    @Getter
    private final RegionResidency residency;

    /** Map of player ID to time stamp of when their invite ends, not serialized */
    @Getter
    private final Map<UUID, ObjectLongPair<UUID>> invites = new Object2ObjectOpenHashMap<>();

    /* ----------------------------- CONSTRUCTORS ------------------------------ */

    PopulationRegion(RegionPos pos) {
        this.pos = pos;
        this.residency = new RegionResidency(this);
    }

    public PopulationRegion(RegionPos pos, CompoundTag tag) {
        this(pos);

        load(tag);

        if(poleBounds == null) {
            updatePoleBounds();
        }
    }

    /* ----------------------------- SERIALIZATION ------------------------------ */

    public void save(CompoundTag tag) {
        if (polePosition != null) {
            tag.put("polePosition", Vectors.writeTag(polePosition));
        }

        tag.put("poleBounds", poleBounds.save());

        if (!Util.isNullOrBlank(name)) {
            tag.putString("name", name);
        }

        if (!properties.isEmpty()) {
            tag.putInt("properties", RegionProperty.pack(properties));
        }

        if(!residency.isEmpty()) {
            tag.put("residency", residency.save());
        }
    }

    public void load(CompoundTag tags) {
        //Set pole position
        if (tags.contains("polePosition")) {
            Vector2i pos = Vectors.read2i(tags.get("polePosition"));
            this.polePosition = pos;
        }

        if (tags.contains("poleBounds")) {
            poleBounds = Bounds3i.of(tags.get("poleBounds"));
        } else {
            updatePoleBounds();
        }

        // Properties must be deserialized before name
        if (tags.contains("properties")) {
            properties.clear();
            properties.addAll(RegionProperty.unpack(tags.getInt("properties")));
        }

        if (tags.contains("name")) {
            this.name = tags.getString("name");
        }

        if (tags.contains("residency")) {
            residency.load(tags.getList("residency", Tag.TAG_COMPOUND));
        }
    }

    /* ----------------------------- METHODS ------------------------------ */

    // sets the pole position without generating a new pole or removing the old one
    protected void setPolePosition0(@Nullable Vector2i polePosition) {
        this.polePosition = polePosition;
        updatePoleBounds();
    }

    @Override
    public boolean hasProperty(RegionProperty property) {
        return properties.contains(property);
    }

    public void addProperty(RegionProperty property) {
        if (hasProperty(property)) {
            return;
        }

        properties.add(property);
        property.onAdd(this);
    }

    public void removeProperty(RegionProperty property) {
        if (!hasProperty(property)) {
            return;
        }

        properties.remove(property);
        property.onRemove(this);
    }

    public void setProperty(RegionProperty property, boolean state) {
        if (state) {
            addProperty(property);
        } else {
            removeProperty(property);
        }
    }

    @Override
    public @NotNull HoverEvent<Component> asHoverEvent(@NotNull UnaryOperator<Component> op) {
        Vector2i vec2 = getPolePosition();

        return HoverEvent.showText(
                op.apply(
                        Component.text()
                                .append(Component.text("x: " + vec2.x()))
                                .append(Component.newline())
                                .append(Component.text("z: " + vec2.y()))
                                .build()
                )
        );
    }

    /**
     * Gets either the name or a string representation of the position
     * @return The region's name, or it's position.
     */
    public String nameOrPos() {
        if (hasName()) {
            return getName();
        }

        Vector2i polePos = getPolePosition();
        return polePos.x() + " " + polePos.y();
    }

    /**
     * Gets the absolute position of this region's pole
     * @return This region's pole position
     */
    public @NotNull Vector2i getPolePosition() {
        return polePosition == null ? pos.toCenter() : polePosition;
    }

    /**
     * Gets the string used for marker ID's.
     * Will always return a non null string.
     * <p></p>
     * Example: region_pole_1_4
     * @return This region's marker ID
     */
    public String getMarkerID() {
        return "region_pole_" + getPos().getX() + "_" + getPos().getZ();
    }

    /**
     * Sets the position of this region's pole.
     * @param polePosition The new pole position.
     */
    public void setPolePosition(@Nullable Vector2i polePosition) {
        //prev must be created before polePosition0 is called
        WorldBounds3i prev = poleBounds.toWorldBounds(
                Regions.getWorld()
        );

        // Actually sets the pole's position and updates the
        // pole's bounding box
        setPolePosition0(polePosition);

        // But the pole must be destroyed after the pole position is set
        // If this is done before setPolePosition0 is called, the
        // generator will think the poleBottom position is below
        // where it actually is, since we just destroyed the pole
        prev.forEach(b -> b.setType(Material.AIR, false));

        // If the marker isn't null and we're allowed to have marker, move it
        if (hasName() && !hasProperty(RegionProperty.FORBIDS_MARKER)) {
            Vector2i pos = polePosition == null ? getPos().toCenter() : polePosition;

            Marker marker = FtcDynmap.getMarker(this);

            if (marker != null) {
                marker.setLocation(
                        Regions.getWorld()
                                .getName(),

                        pos.x() + 0.5D,
                        this.getPoleBounds().maxY(),
                        pos.y() + 0.5D
                );
            }
        }

        Regions.placePole(this);
    }

    //Updates the pole's bounding box
    protected void updatePoleBounds() {
        poleBounds = Regions.poleBoundingBox(this);
    }

    /**
     * Tests whether the region should be serialized
     * <p>
     * Whether a region has a description doesn't get counted, as without a name, a description
     * is useless for regions.
     *
     * @return True if the region either has a name or a custom pole position, false otherwise
     */
    public boolean isImportant() {
        return !residency.isEmpty() || !Util.isNullOrBlank(name) || polePosition != null;
    }

    /**
     * Gets the pole's bottom position
     * @return The pole's bottom position
     */
    public Vector3i getPoleBottom() {
        return getPoleBounds()
                .center()
                .toInt()
                .withY(getPoleBounds().minY());
    }

    /* ----------------------------- INVITES ------------------------------ */

    public void invite(UUID inviter, UUID uuid) {
        invites.put(
                uuid,
                ObjectLongPair.of(inviter, System.currentTimeMillis() + GeneralConfig.validInviteTime)
        );
    }

    public UUID getInviter(UUID recipient) {
        if (!hasValidInvite(recipient)) {
            return null;
        }

        return invites.get(recipient).first();
    }

    public boolean hasValidInvite(UUID uuid) {
        long l = invites.get(uuid).rightLong();

        if (Time.isPast(l)) {
            invites.remove(uuid);
            return false;
        } else {
            return true;
        }
    }
}