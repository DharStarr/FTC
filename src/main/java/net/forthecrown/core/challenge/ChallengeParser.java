package net.forthecrown.core.challenge;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import net.forthecrown.core.FTC;
import net.forthecrown.core.script.ScriptManager;
import net.forthecrown.core.script.ScriptResult;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.Results;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;

public class ChallengeParser {
    public static final String
            KEY_NAME = "displayName",
            KEY_DESC = "description",

            KEY_SCRIPT = "script",
            KEY_EVENT_CLASS = "eventClass",

            KEY_RESET_INTERVAL = "type",

            KEY_REWARD = "rewards",
            KEY_R_RHINES = "rhines",
            KEY_R_GEMS = "gems",
            KEY_R_ITEM = "item",
            KEY_R_GUILD = "guildExp",

            KEY_GOAL = "goal",

            EVENT_CUSTOM = "custom";

    public static DataResult<JsonChallenge> parse(JsonObject object) {
        JsonWrapper json = JsonWrapper.wrap(object);

        if (!json.has(KEY_NAME)
                || !json.has(KEY_EVENT_CLASS)
        ) {
            return Results.errorResult(
                    "Missing one of the following fields: %s, %s",
                    KEY_NAME, KEY_EVENT_CLASS
            );
        }

        JsonChallenge.Builder builder = JsonChallenge.builder()
                .name(json.getComponent(KEY_NAME))
                .goal(json.getFloat(KEY_GOAL, 1))
                .script(json.getString(KEY_SCRIPT, ""))

                .resetInterval(json.getEnum(
                        KEY_RESET_INTERVAL,
                        ResetInterval.class,
                        ResetInterval.DAILY
                ));

        // No need to test if object contains description, getList returns
        // an empty list if it doesn't
        for (var c: json.getList(KEY_DESC, JsonUtils::readText)) {
            builder.addDesc(c);
        }

        // Name of the event class this challenge will listen to,
        // will be EVENT_CUSTOM, empty or null, if it expects to
        // be called from within FTC code and not listen to a
        // bukkit event
        String className = json.getString(KEY_EVENT_CLASS);

        if (!Strings.isNullOrEmpty(className)
                && !className.equalsIgnoreCase(EVENT_CUSTOM)
        ) {
            try {
                Class eventClass = Class.forName(
                        className, true,

                        FTC.getPlugin()
                                .getClass()
                                .getClassLoader()
                );

                if (!Event.class.isAssignableFrom(eventClass)) {
                    return Results.errorResult(
                            "Class '%s' is not a sub class of '%s'",
                            eventClass.getName(), Event.class.getName()
                    );
                }

                builder.eventClass(eventClass);

                if (Strings.isNullOrEmpty(builder.script())
                        && !PlayerEvent.class.isAssignableFrom(eventClass)
                        && eventClass != PlayerDeathEvent.class
                ) {
                    return Results.errorResult(
                            "No script specified and given event (%s) " +
                                    "was not a player event",

                            eventClass.getName()
                    );
                }
            } catch (ClassNotFoundException e) {
                return Results.errorResult(
                        "Class '%s' not found",
                        className
                );
            }
        } else {
            if (Strings.isNullOrEmpty(builder.script())) {
                return DataResult.error(
                        "Custom callback was given, however no script was " +
                                "specified to handle it"
                );
            }
        }

        if (json.has(KEY_REWARD)) {
            var rewards = json.getWrapped(KEY_REWARD);

            builder.reward(
                    JsonReward.builder()
                            .gems(rewards.getInt(KEY_R_GEMS, 0))
                            .rhines(rewards.getInt(KEY_R_RHINES, 0))
                            .guildExp(rewards.getInt(KEY_R_GUILD))
                            .item(rewards.getItem(KEY_R_ITEM, null))
                            .build()
            );
        }

        if (Strings.isNullOrEmpty(builder.script())) {
            return build(builder, null);
        }

        return readListener(builder.script())
                .toDataResult()
                .flatMap(engine -> build(builder, engine));
    }

    static ScriptResult readListener(String script) {
        return ScriptManager.getInstance()
                .readAndRunScript(script);
    }

    static DataResult<JsonChallenge> build(JsonChallenge.Builder builder,
                                           NashornScriptEngine engine
    ) {
        JsonChallenge challenge = builder.build();
        ChallengeHandle handle = new ChallengeHandle(challenge);
        challenge.listener = new ScriptEventListener(engine, handle);

        return DataResult.success(challenge);
    }
}