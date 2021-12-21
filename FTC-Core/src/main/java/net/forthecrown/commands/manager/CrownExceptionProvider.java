package net.forthecrown.commands.manager;

import net.forthecrown.grenadier.exceptions.TranslatableExceptionType;

/**
 * Class for storing exception constants
 */
class CrownExceptionProvider {
    static final TranslatableExceptionType REGIONS_WRONG_WORLD =        new TranslatableExceptionType("regions.wrongWorld");

    static final TranslatableExceptionType NO_REPLY_TARGETS =           new TranslatableExceptionType("commands.noReply");

    static final TranslatableExceptionType CANNOT_AFFORD_TRANSACTION =  new TranslatableExceptionType("commands.cannotAfford");
    static final TranslatableExceptionType CANNOT_AFFORD_INFOLESS =     new TranslatableExceptionType("commands.cannotAffordInfoless");

    static final TranslatableExceptionType SENDER_EMOTE_DISABLED =      new TranslatableExceptionType("emotes.disabled.sender");
    static final TranslatableExceptionType TARGET_EMOTE_DISABLED =      new TranslatableExceptionType("emotes.disabled.target");

    static final TranslatableExceptionType SENDER_TPA_DISABLED =        new TranslatableExceptionType("commands.tpaDisabled.sender");
    static final TranslatableExceptionType TARGET_TPA_DISABLED =        new TranslatableExceptionType("commands.tpaDisabled.target");

    static final TranslatableExceptionType SENDER_PAY_DISABLED =        new TranslatableExceptionType("commands.payDisabled.sender");
    static final TranslatableExceptionType TARGET_PAY_DISABLED =        new TranslatableExceptionType("commands.payDisabled.target");
    static final TranslatableExceptionType CANNOT_PAY_SELF =            new TranslatableExceptionType("commands.cannotPaySelf");

    static final TranslatableExceptionType CANNOT_TELEPORT =            new TranslatableExceptionType("commands.cannotTeleport");
    static final TranslatableExceptionType CANNOT_TP_TO_SELF =          new TranslatableExceptionType("commands.cannotTpToSelf");

    static final TranslatableExceptionType NO_TP_REQUESTS_INFOLESS =    new TranslatableExceptionType("commands.noTpReqInfoless");
    static final TranslatableExceptionType NO_TP_INCOMING =             new TranslatableExceptionType("commands.noTpIncoming");
    static final TranslatableExceptionType NO_TP_OUTGOING =             new TranslatableExceptionType("commands.noTpOutgoing");
    static final TranslatableExceptionType ALREADY_SENT =               new TranslatableExceptionType("commands.tpaAlreadySent");

    static final TranslatableExceptionType NICK_TOO_LONG =              new TranslatableExceptionType("commands.nickTooLong");

    static final TranslatableExceptionType MUST_BE_HOLDING_ITEM =       new TranslatableExceptionType("commands.mustHoldItem");

    static final TranslatableExceptionType NO_RETURN =                  new TranslatableExceptionType("commands.noBackLoc");
    static final TranslatableExceptionType ALREADY_BARON =              new TranslatableExceptionType("commands.becomeBaron.alreadyBaron");
    static final TranslatableExceptionType HOLDING_COINS =              new TranslatableExceptionType("commands.holdCoins");

    static final TranslatableExceptionType INV_FULL =                   new TranslatableExceptionType("commands.invFull");
    static final TranslatableExceptionType NO_ONE_NEARBY =              new TranslatableExceptionType("commands.noOneNearby");

    static final TranslatableExceptionType BLOCKED_PLAYER =             new TranslatableExceptionType("user.blocked");
    static final TranslatableExceptionType IGNORE_SELF_NO =             new TranslatableExceptionType("user.cannotIgnoreSelf");
    static final TranslatableExceptionType CANNOT_PAY_BLOCKED =         new TranslatableExceptionType("user.cannotPayBlocked");

    static final TranslatableExceptionType NO_HOMES =                   new TranslatableExceptionType("homes.noneToList");
    static final TranslatableExceptionType NO_DEF_HOME =                new TranslatableExceptionType("homes.noDefaultHome");
    static final TranslatableExceptionType OVER_HOME_LIMIT =            new TranslatableExceptionType("homes.overLimit");
    static final TranslatableExceptionType CANNOT_TP_HOME =             new TranslatableExceptionType("homes.badWorld");
    static final TranslatableExceptionType CANNOT_SET_HOME =            new TranslatableExceptionType("homes.cannotSetHere");

    static final TranslatableExceptionType CANNOT_TPA =                 new TranslatableExceptionType("tpa.no");
    static final TranslatableExceptionType CANNOT_TPA_HERE =            new TranslatableExceptionType("tpa.noHere");
    static final TranslatableExceptionType CANNOT_RETURN =              new TranslatableExceptionType("commands.cannotReturn");

    static final TranslatableExceptionType MARRIED_SENDER =             new TranslatableExceptionType("marriage.alreadyMarried.sender");
    static final TranslatableExceptionType MARRIED_TARGET =             new TranslatableExceptionType("marriage.alreadyMarried.target");
    static final TranslatableExceptionType MARRIAGE_CANNOT_CHANGE =     new TranslatableExceptionType("marriage.cannotChange.sender");
    static final TranslatableExceptionType MARRIAGE_CANNOT_CHANGE_T =   new TranslatableExceptionType("marriage.cannotChange.target");
    static final TranslatableExceptionType NOT_MARRIED =                new TranslatableExceptionType("marriage.notMarried");

    static final TranslatableExceptionType NOT_PIRATE =                 new TranslatableExceptionType("pirates.exclusive");
    static final TranslatableExceptionType GOTTA_BE_PIRATE =            new TranslatableExceptionType("pirates.wrongBranch");

    static final TranslatableExceptionType SHOP_OUT_OF_STOCK =          new TranslatableExceptionType("shops.error.outOfStock");
    static final TranslatableExceptionType SHOP_DONT_HAVE_ITEM =        new TranslatableExceptionType("shops.error.dontHaveItem");
    static final TranslatableExceptionType SHOP_OWNER_CANNOT_AFFORD =   new TranslatableExceptionType("shops.error.ownerCannotAfford");
    static final TranslatableExceptionType SHOP_NO_SPACE =              new TranslatableExceptionType("shops.error.noSpace");
    static final TranslatableExceptionType SHOP_PRICE_EXCEEDED =        new TranslatableExceptionType("shops.created.failed.maxPrice");
}