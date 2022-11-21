function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // "Custom" event should only triggered when buying
    // in a shop they haven't shopped in before (today).
    handle.givePoint(event.getPlayer());
}

function onComplete(user) {
    logger.warn("Buy challenge completed by: {}", user.getName())
}