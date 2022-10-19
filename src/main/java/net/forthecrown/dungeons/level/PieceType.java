package net.forthecrown.dungeons.level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.dungeons.level.gate.GateData;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.Structures;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.forthecrown.dungeons.level.gate.GateData.*;

@RequiredArgsConstructor
public abstract class PieceType<T extends DungeonPiece> {
    @Getter
    private final String structureName;

    public abstract T create();
    public abstract T load(CompoundTag tag);

    public Optional<BlockStructure> getStructure() {
        return Structures.get()
                .getRegistry()
                .get(structureName);
    }

    public List<GateData> getGates() {
        return getStructure().map(structure -> {
            List<GateData> result = new ObjectArrayList<>();

            structure.getFunctions()
                    .stream()
                    // Filter non gate functions
                    .filter(info -> {
                        return info.getFacing().isRotatable()
                                && info.getFunctionKey().equals(LevelFunctions.CONNECTOR);
                    })

                    // Load gate data
                    .map(info -> {
                        GateData.Opening opening = GateData.DEFAULT_OPENING;
                        Vector3i offset = info.getOffset();
                        boolean applyCorrection = true;
                        boolean stairs = false;

                        if (info.getTag() != null && !info.getTag().isEmpty()) {
                            var tag = info.getTag();
                            opening = GateData.Opening.load(tag.get(TAG_OPENING));

                            // If value not given or value is positive, otherwise,
                            // meaning the value is given and set to false, so set
                            // variable to false too
                            applyCorrection = !tag.contains(TAG_CORRECT)
                                    || tag.getBoolean(TAG_CORRECT);

                            stairs = !tag.contains(TAG_STAIR)
                                    || tag.getBoolean(TAG_STAIR);
                        }

                        // Correction here means to move the entrance out block outward
                        // and downward to compensate for the fact the function block is
                        // placed on the floor not inside it and isn't one block outside
                        // the structure itself
                        if (applyCorrection) {
                            offset = offset.add(info.getFacing().getMod())
                                    .sub(0, 1, 0);
                        }

                        return new GateData(info.getFacing(), offset, stairs, opening);
                    })

                    // Populate list
                    .forEach(result::add);

            return result.isEmpty() ? null : result;
        }).orElse(Collections.emptyList());
    }
}