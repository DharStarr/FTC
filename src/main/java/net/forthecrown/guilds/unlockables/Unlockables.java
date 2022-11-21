package net.forthecrown.guilds.unlockables;

import lombok.experimental.UtilityClass;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;

public @UtilityClass class Unlockables {
    public final Registry<Unlockable> REGISTRY = Registries.newFreezable();

    private static void init() {
        registerAll(UnlockableChunkUpgrade.values());
        registerAll(UnlockableColor.values());
        registerAll(UnlockableRankSlot.values());
        registerAll(Upgradable.values());
        registerAll(UnlockableSetting.values());
    }

    private static void registerAll(Unlockable[] unlockables) {
        for (var u: unlockables) {
            REGISTRY.register(u.getKey(), u);
        }
    }
}