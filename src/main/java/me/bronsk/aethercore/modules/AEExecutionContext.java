package me.bronsk.aethercore.modules;

import java.util.UUID;

public record AEExecutionContext(UUID actorUuid, String actorName, String lang, boolean isPlayer) {

    public static AEExecutionContext console(String lang) {
        return new AEExecutionContext(null, "console", lang, false);
    }

    public static AEExecutionContext player(UUID actorUuid, String actorName, String lang) {
        return new AEExecutionContext(actorUuid, actorName, lang, true);
    }

}
