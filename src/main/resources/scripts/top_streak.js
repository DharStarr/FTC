// Imports
const Worlds = Java.type("net.forthecrown.core.Worlds");
const StreakIncreaseEvent = Java.type("net.forthecrown.core.challenge.StreakIncreaseEvent");
const Challenges = Java.type("net.forthecrown.core.challenge.Challenges");
const C_Manager = Java.type("net.forthecrown.core.challenge.ChallengeManager");
const ArmorStand = Java.type("org.bukkit.entity.ArmorStand");
const UnitFormat = Java.type("net.forthecrown.utils.text.format.UnitFormat");
const StreakCategory = Java.type("net.forthecrown.core.challenge.StreakCategory");
const DynamicArmorStand = Java.type("net.forthecrown.utils.stand.DynamicArmorStand");

// Constants
const STAND_POSITION = Vector3d.from(207.5, 73.15, 188.5);
const NO_STREAK = 0;

const dynamicStand = new DynamicArmorStand(
    new Location(Worlds.overworld(), 207.5, 73.15, 188.5)
);

// Fields
let greatestId = null;
let greatestStreak = NO_STREAK;

events.register("onStreakIncrease", StreakIncreaseEvent);
scanInitial();
updateStand();

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

    updateStand();
}

function updateStand() {
    if (greatestId == null || greatestStreak < 0) {
        dynamicStand.kill();
    }

    dynamicStand.update(createStandName(greatestId, greatestStreak));
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

// Called when the script is closed
function __onClose() {
    dynamicStand.kill();
}

// Formats the armor stand's name
function createStandName(playerId, streak) {
    return Text.format("{0, user}: &e{1}", playerId, UnitFormat.unit(streak, "Day"));
}