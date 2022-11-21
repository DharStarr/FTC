package net.forthecrown.core.challenge;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.FTC;
import net.forthecrown.core.script.Scripts;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static net.forthecrown.core.challenge.Challenges.*;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

@Getter
public class JsonChallenge implements Challenge {
    private final Component name;
    private final ImmutableList<Component> description;

    private final Class<Event> eventClass;
    private final String script;

    private final float goal;

    private final ResetInterval resetInterval;

    private final JsonReward reward;

    ScriptEventListener listener;
    boolean listenerRegistered = false;

    /* ---------------------------- CONSTRUCTOR ----------------------------- */

    private JsonChallenge(Builder builder) {
        this.name = requireNonNull(builder.name, "Name null");
        this.description = requireNonNull(builder.description, "Desc null")
                .build();

        this.eventClass = builder.eventClass;
        this.script = requireNonNull(builder.script, "Script null");

        this.goal = builder.goal;

        this.resetInterval = requireNonNull(builder.resetInterval,
                "Reset interval null"
        );

        this.reward = requireNonNull(builder.reward, "Reward null");
    }

    /* ------------------------------ METHODS ------------------------------- */

    public Component displayName() {
        TextComponent.Builder hoverBuilder = text();
        var it = description.iterator();

        while (it.hasNext()) {
            hoverBuilder.append(it.next());

            if (it.hasNext()) {
                hoverBuilder.append(newline());
            }
        }

        if (!reward.isEmpty()) {
            var writer = TextWriters.wrap(hoverBuilder);
            writer.newLine();
            writer.newLine();

            writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
            writer.setFieldValueStyle(Style.style(NamedTextColor.GRAY));

            reward.write(writer);
        }

        return name
                .color(NamedTextColor.YELLOW)
                .hoverEvent(hoverBuilder.build());
    }

    @Override
    public String activate() {
        registerListener();

        if (getListener().getScript() != null) {
            getListener().getScript()
                    .invoke(METHOD_ON_ACTIVATE, getListener().getHandle());
        }

        return "";
    }

    @Override
    public void deactivate() {
        unregisterListener();

        if (getListener().getScript() != null) {
            getListener().getScript()
                    .invoke(METHOD_ON_RESET, getListener().getHandle());
        }
    }

    public void registerListener() {
        if (listenerRegistered) {
            return;
        }

        reloadListener();

        if (eventClass != null) {
            Bukkit.getPluginManager()
                    .registerEvent(
                            eventClass,
                            listener,
                            EventPriority.NORMAL,
                            listener,
                            FTC.getPlugin(),
                            true
                    );
        }

        listenerRegistered = true;
    }

    public void unregisterListener() {
        if (!listenerRegistered) {
            return;
        }

        if (eventClass != null) {
            HandlerList.unregisterAll(listener);
        }

        listenerRegistered = false;
    }

    public void reloadListener() {
        if (Strings.isNullOrEmpty(script)) {
            listener.script = null;
            return;
        }

        listener.script = Scripts.read(script);
    }

    @Override
    public boolean canComplete(User user) {
        if (getListener().getScript() == null) {
            return true;
        }

        if (!getListener().getScript().hasMethod(METHOD_CAN_COMPLETE)) {
            return true;
        }

        var result = getListener().getScript()
                .invoke(METHOD_CAN_COMPLETE, user);

        return result
                .resultAsBoolean()
                .orElse(false);
    }

    @Override
    public void onComplete(User user) {
        if (getListener().getScript() != null) {
            getListener().getScript()
                    .invoke(METHOD_ON_COMPLETE, user);
        }

        if (!reward.isEmpty()) {
            reward.give(user);
        }
    }

    @Override
    public void trigger(Object input) {
        if (input instanceof Player
                && eventClass == null
                && Strings.isNullOrEmpty(script)
        ) {
            listener.getHandle().givePoint(input);
            return;
        }

        if (getListener().getScript() == null) {
            FTC.getLogger().error(
                    "Cannot manually invoke script {}! No script set",
                    getListener().getScript()
            );

            return;
        }

        if (eventClass != null) {
            FTC.getLogger().error(
                    "Cannot manually invoke script {}! Event class has "
                            + "been specified!",
                    getListener().getScript()
            );

            return;
        }

        if (!getListener().getScript().hasMethod(METHOD_ON_EVENT)) {
            FTC.getLogger().error(
                    "Cannot manually invoke script {}! No onEvent method set",
                    getListener().getScript()
            );

            return;
        }

        getListener().getScript().invoke(
                METHOD_ON_EVENT,
                input,
                getListener().getHandle()
        );
    }

    /* -------------------------- OBJECT OVERRIDES -------------------------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof JsonChallenge challenge)) {
            return false;
        }

        return getGoal() == challenge.getGoal()
                && getName().equals(challenge.getName())
                && getEventClass().equals(challenge.getEventClass())
                && Objects.equals(getScript(), challenge.getScript())
                && getResetInterval() == challenge.getResetInterval();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getName(),
                getEventClass(),
                getScript(),
                getGoal(),
                getResetInterval()
        );
    }

    /* ------------------------------ BUILDER ------------------------------- */

    public static Builder builder() {
        return new Builder();
    }

    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    public static class Builder {
        private Component name;

        private final ImmutableList.Builder<Component>
                description = ImmutableList.builder();

        private Class<Event> eventClass;
        private String script;

        private JsonReward reward = JsonReward.EMPTY;
        private ResetInterval resetInterval = ResetInterval.DAILY;

        private float goal = 1;

        public Builder addDesc(Component text) {
            description.add(text);
            return this;
        }

        public JsonChallenge build() {
            return new JsonChallenge(this);
        }
    }
}