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

// Fields
let greatestId = null;
let greatestStreak = -1;

let standId = null;

events.register("onStreakIncrease", StreakIncreaseEvent);
scanInitial();
spawnStand();

function onStreakIncrease(/* StreakIncreaseEvent */ event) {
    if (greatestId != null && greatestStreak >= event.getStreak()) {
        return;
    }

    updateStreak(event.getUser().getUniqueId(), event.getStreak());
}

function updateStreak(playerId, streak) {
    let stand = findStand();

    greatestId = playerId;
    greatestStreak = streak;

    if (stand == null) {
        return;
    }

    stand.customName(createStandName(playerId, streak));
}

function scanInitial() {
    let entries = C_Manager.getInstance().getEntries();

    entries.forEach(e => {
        const streak = Challenges.queryStreak(StreakCategory.ITEMS, e.getUser()).orElse(0);

        if (streak <= 0) {
            return;
        }

        if (greatestId == null || greatestStreak < streak) {
            greatestId = e.getId();
            greatestStreak = streak;
        }
    });
}

function findStand() {
    if (standId == null) {
        spawnStand();
    }

    return Worlds.overworld()
            .getEntity(standId);
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
    if (greatestStreak == -1 || greatestId == null) {
        return;
    }

    let location = new Location(Worlds.overworld(), STAND_POSITION.x(), STAND_POSITION.y(), STAND_POSITION.z());
    let marker =  Worlds.overworld().spawn(location, ArmorStand, stand => {
        stand.setMarker(true);
        stand.setBasePlate(false);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setCustomNameVisible(true);
        stand.setCanTick(false);

        stand.customName(createStandName(greatestId, greatestStreak));
    });

    standId = marker.getUniqueId();
}

// Formats the armor stand's name
function createStandName(playerId, streak) {
    return Text.format("{0, user}: &e{1}", playerId, UnitFormat.unit(streak, "Day"));
}