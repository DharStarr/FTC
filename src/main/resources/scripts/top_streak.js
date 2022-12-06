// Imports
const Worlds = Java.type("net.forthecrown.core.Worlds");
const StreakIncreaseEvent = Java.type("net.forthecrown.core.challenge.StreakIncreaseEvent");
const Challenges = Java.type("net.forthecrown.core.challenge.Challenges");
const C_Manager = Java.type("net.forthecrown.core.challenge.ChallengeManager");
const ArmorStand = Java.type("org.bukkit.entity.ArmorStand");
const UnitFormat = Java.type("net.forthecrown.utils.text.format.UnitFormat");
const StreakCategory = Java.type("net.forthecrown.core.challenge.StreakCategory");

// Constants
const STAND_POSITION = Vector3d.from(207.5, 73.15, 188.5);
const NO_STREAK = 0;

// Fields
let greatestId = null;
let greatestStreak = NO_STREAK;

let standId = null;
let standArea = null;

events.register("onStreakIncrease", StreakIncreaseEvent);
scanInitial();
spawnStand();

function onStreakIncrease(/* StreakIncreaseEvent */ event) {
    if (event.getCategory() != StreakCategory.ITEMS) {
        return;
    }

    let highestStreak = event.getEntry().getHighestStreak();

    if (greatestId != null && greatestStreak >= highestStreak) {
        return;
    }

    updateStreak(event.getUser().getUniqueId(), highestStreak);
}

function updateStreak(playerId, streak) {
    greatestId = playerId;
    greatestStreak = streak;

    let stand = findStand();

    if (stand == null) {
        return;
    }

    stand.customName(createStandName(playerId, streak));
}

function scanInitial() {
    let entries = C_Manager.getInstance().getEntries();

    entries.forEach(e => {
        let streak = e.getHighestStreak();

        if (streak <= NO_STREAK) {
            return;
        }

        if (greatestId == null || greatestStreak < streak) {
            greatestId = e.getId();
            greatestStreak = streak;
        }
    });
}

function findStand() {
    let stand = null;

    if (standId != null) {
        stand = Worlds.overworld().getEntity(standId);
    }

    if (stand == null && standArea != null && standId != null) {
        let entities = Worlds.overworld()
                .getNearbyEntities(standArea, entity => entity.getUniqueId().equals(standId));

        if (!entities.isEmpty()) {
            stand = entities.iterator().next();
        }
    }

    if (stand != null) {
        return stand;
    }

    return spawnStand();
}

// Called when the script is closed
function __onClose() {
    if (standId == null) {
        return;
    }

    let stand = findStand();

    if (stand != null) {
        stand.remove();
    }
}

function spawnStand() {
    if (greatestStreak == NO_STREAK || greatestId == null) {
        return;
    }

    let location = new Location(Worlds.overworld(), STAND_POSITION.x(), STAND_POSITION.y(), STAND_POSITION.z());
    let marker =  Worlds.overworld().spawn(location, ArmorStand.class, stand => {
        stand.setMarker(true);
        stand.setBasePlate(false);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setCustomNameVisible(true);
        stand.setCanTick(false);

        stand.customName(createStandName(greatestId, greatestStreak));
    });

    standId = marker.getUniqueId();
    standArea = marker.getBoundingBox();

    return marker;
}

// Formats the armor stand's name
function createStandName(playerId, streak) {
    return Text.format("{0, user}: &e{1}", playerId, UnitFormat.unit(streak, "Day"));
}