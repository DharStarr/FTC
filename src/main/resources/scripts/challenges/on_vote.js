function canComplete(user) {
    return user.getGuild() != null;
}

function onEvent(event, handle) {
    // PlayerPostVoteEvent
    handle.givePoint(event.getUser().getPlayer());
}

function onComplete(user) {
    logger.warn("Vote challenge completed by: {}", user.getName())
}