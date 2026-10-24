package me.bronsk.aethercore.modules;

import com.hypixel.hytale.server.core.Message;

/**
 * Wraps a handler's outcome for UI consumers: the message a command would print, plus whether the
 * action actually succeeded - {@link Message} alone carries no such concept. {@code message} may be
 * {@code null} in the rare "actor itself is unresolvable" branches, mirroring the null-tolerance
 * {@code sendToActor} already had.
 */
public record AEActionResult(Message message, boolean success) {

    public static AEActionResult success(Message message) {
        return new AEActionResult(message, true);
    }

    public static AEActionResult error(Message message) {
        return new AEActionResult(message, false);
    }
}
