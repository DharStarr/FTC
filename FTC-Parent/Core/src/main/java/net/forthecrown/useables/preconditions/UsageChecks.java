package net.forthecrown.useables.preconditions;

import net.forthecrown.core.CrownCore;

import static net.forthecrown.registry.Registries.USAGE_CHECKS;

public class UsageChecks {
    public static void init(){
        register(new CheckCooldownType());
        register(new CheckHasAllItemsType());

        register(new CheckHasItemType());
        register(new CheckHasScoreType());

        register(new CheckInWorld());

        register(new CheckNeverUsed());
        register(new CheckNotUsedBefore());

        register(new CheckRankType());
        register(new CheckBranchType());
        register(new CheckPermission());

        register(new CheckNumber(true));
        register(new CheckNumber(false));

        register(new SimpleCheckType(CheckInventoryEmpty::new, CheckInventoryEmpty.KEY));
        register(new SimpleCheckType(CheckIsNotAlt::new, CheckIsNotAlt.KEY));

        USAGE_CHECKS.close();
        CrownCore.logger().info("Default checks registered");
    }

    private static void register(UsageCheck<?> check){
        USAGE_CHECKS.register(check.key(), check);
    }
}