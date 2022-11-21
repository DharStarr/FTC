function canComplete(user) {
    return user.getGuild() != null;
}

function onActivate(handle) {
    Users.getOnline().forEach(function f(user) {
        handle.givePoint(user);
    });
}