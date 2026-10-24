package me.bronsk.aethercore.utils;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.server.core.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AEMessageUtils {

    private static final Map<Character, String> MINECRAFT_COLORS = Map.ofEntries(
            Map.entry('0', "#000000"),
            Map.entry('1', "#0000aa"),
            Map.entry('2', "#00aa00"),
            Map.entry('3', "#00aaaa"),
            Map.entry('4', "#aa0000"),
            Map.entry('5', "#aa00aa"),
            Map.entry('6', "#ffaa00"),
            Map.entry('7', "#aaaaaa"),
            Map.entry('8', "#555555"),
            Map.entry('9', "#5555ff"),
            Map.entry('a', "#55ff55"),
            Map.entry('b', "#55ffff"),
            Map.entry('c', "#ff5555"),
            Map.entry('d', "#ff55ff"),
            Map.entry('e', "#ffff55"),
            Map.entry('f', "#ffffff"));
    private static final Map<String, Character> COLOR_CODES = createColorCodes();

    /**
     * Creates a new instance.
     */
    private AEMessageUtils() {
    }

    /**
     * Handles fromColoredStringToMessage.
     *
     * @param input Method argument.
     * @return Method result.
     */
    public static Message fromColoredStringToMessage(String input) {
        if (input == null || input.isEmpty()) {
            return Message.empty();
        }

        List<Message> parts = new ArrayList<>();
        FormattingState state = new FormattingState();
        StringBuilder text = new StringBuilder();

        int index = 0;
        while (index < input.length()) {
            char current = input.charAt(index);
            if (current == '&') {
                int nextIndex = applyAmpersandCode(input, index, parts, text, state);
                if (nextIndex != index) {
                    index = nextIndex;
                    continue;
                }
            }

            text.append(current);
            index++;
        }

        addPart(parts, text.toString(), state);
        if (parts.isEmpty()) {
            return Message.empty();
        }

        return Message.join(parts.toArray(Message[]::new));
    }

    /**
     * Handles fromColoredString.
     *
     * @param input Method argument.
     * @return Method result.
     */
    public static Message fromColoredString(String input) {
        return fromColoredStringToMessage(input);
    }

    /**
     * Handles fromMessageToColoredString.
     *
     * @param message Method argument.
     * @return Method result.
     */
    public static String fromMessageToColoredString(Message message) {
        if (message == null) {
            return "";
        }

        StringBuilder output = new StringBuilder();
        appendMessage(output, message, new FormattingState());
        return output.toString();
    }

    public static String escapeFormatting(String input) {
        return input == null ? "" : input.replace("&", "&&");
    }

    /**
     * Flattens a {@link Message} tree into plain text with no formatting codes - unlike {@link
     * #fromMessageToColoredString}, which emits {@code &}-codes a native UI {@code Label.Text}
     * property can't interpret. Reuses whatever {@link Message} a module handler already built for
     * chat - no separate copy needed to show it in the notification banner.
     *
     * @param message Message to flatten.
     * @return Plain concatenated text.
     */
    public static String toPlainText(Message message) {
        if (message == null) {
            return "";
        }

        StringBuilder output = new StringBuilder();
        appendPlainText(output, message);
        return output.toString();
    }

    private static void appendPlainText(StringBuilder output, Message message) {
        String rawText = message.getRawText();
        if (rawText != null) {
            output.append(rawText);
        }
        for (Message child : message.getChildren()) {
            appendPlainText(output, child);
        }
    }

    /**
     * Adds data through addPart.
     *
     * @param parts Method argument.
     * @param text Method argument.
     * @param state Method argument.
     */
    private static void addPart(List<Message> parts, String text, FormattingState state) {
        if (text.isEmpty()) {
            return;
        }

        Message message = Message.raw(text);
        if (state.color != null) {
            message.color(state.color);
        }
        if (state.bold) {
            message.bold(true);
        }
        if (state.italic) {
            message.italic(true);
        }
        if (state.monospace) {
            message.monospace(true);
        }
        parts.add(message);
    }

    /**
     * Applies an ampersand formatting code if one starts at the given index.
     *
     * @param input Source text.
     * @param index Current index.
     * @param parts Message parts.
     * @param text Pending text.
     * @param state Active formatting state.
     * @return Next index when a code was consumed, otherwise the original index.
     */
    private static int applyAmpersandCode(
            String input,
            int index,
            List<Message> parts,
            StringBuilder text,
            FormattingState state) {
        if (index + 1 >= input.length()) {
            return index;
        }

        char code = Character.toLowerCase(input.charAt(index + 1));
        if (code == '&') {
            text.append('&');
            return index + 2;
        }

        if (code == '#' && index + 7 < input.length()) {
            String hex = input.substring(index + 2, index + 8);
            if (isHexColor(hex)) {
                addPart(parts, text.toString(), state);
                text.setLength(0);
                state.color = normalizeColor(hex);
                return index + 8;
            }
        }

        String color = MINECRAFT_COLORS.get(code);
        if (color != null) {
            addPart(parts, text.toString(), state);
            text.setLength(0);
            state.color = color;
            return index + 2;
        }

        if (code == 'l' || code == 'o' || code == 'm' || code == 'r') {
            addPart(parts, text.toString(), state);
            text.setLength(0);
            switch (code) {
                case 'l' -> state.bold = true;
                case 'o' -> state.italic = true;
                case 'm' -> state.monospace = true;
                case 'r' -> state.reset();
                default -> {
                }
            }
            return index + 2;
        }

        return index;
    }

    /**
     * Handles appendMessage.
     *
     * @param output Method argument.
     * @param message Method argument.
     * @param currentState Method argument.
     */
    private static void appendMessage(StringBuilder output, Message message, FormattingState currentState) {
        FormattingState nextState = currentState.copy();
        FormattingState messageState = currentState.copy();
        applyMessageFormatting(message, messageState);
        String rawText = message.getRawText();

        if (rawText != null && !rawText.isEmpty()) {
            appendFormattingCodes(output, currentState, messageState);
            output.append(escapeRawText(rawText));
            nextState = messageState;
        }

        for (Message child : message.getChildren()) {
            appendMessage(output, child, nextState);
            applyMessageFormatting(child, nextState);
        }
    }

    /**
     * Applies the formatting stored in a Message to a state object.
     *
     * @param message Message to inspect.
     * @param state State to mutate.
     */
    private static void applyMessageFormatting(Message message, FormattingState state) {
        String color = message.getColor();
        if (color != null) {
            state.color = normalizeColor(color);
        }

        FormattedMessage formattedMessage = message.getFormattedMessage();
        if (Boolean.TRUE.equals(formattedMessage.bold)) {
            state.bold = true;
        }
        if (Boolean.TRUE.equals(formattedMessage.italic)) {
            state.italic = true;
        }
        if (Boolean.TRUE.equals(formattedMessage.monospace)) {
            state.monospace = true;
        }
    }

    /**
     * Appends the shortest known formatting codes needed to move between states.
     *
     * @param output Output builder.
     * @param currentState Current state.
     * @param nextState Next state.
     */
    private static void appendFormattingCodes(
            StringBuilder output,
            FormattingState currentState,
            FormattingState nextState) {
        if (nextState.isDefault()) {
            if (!currentState.isDefault()) {
                output.append("&r");
            }
            return;
        }

        if (currentState.hasEnabledStyleNotIn(nextState)
                || currentState.hasColor() && !nextState.hasColor()) {
            output.append("&r");
            currentState = new FormattingState();
        }

        if (nextState.color != null && !nextState.color.equals(currentState.color)) {
            Character colorCode = COLOR_CODES.get(nextState.color);
            if (colorCode != null) {
                output.append('&').append(colorCode);
            } else {
                output.append("&#").append(nextState.color.substring(1).toUpperCase(Locale.ROOT));
            }
        }
        if (nextState.bold && !currentState.bold) {
            output.append("&l");
        }
        if (nextState.italic && !currentState.italic) {
            output.append("&o");
        }
        if (nextState.monospace && !currentState.monospace) {
            output.append("&m");
        }
    }

    /**
     * Handles normalizeColor.
     *
     * @param color Method argument.
     * @return Method result.
     */
    private static String normalizeColor(String color) {
        if (color == null) {
            return null;
        }

        String normalized = color.trim().toLowerCase(Locale.ROOT);
        if (!normalized.startsWith("#")) {
            normalized = "#" + normalized;
        }

        return normalized;
    }

    /**
     * Escapes raw text so ampersands are not parsed as formatting codes later.
     *
     * @param rawText Text to escape.
     * @return Escaped text.
     */
    private static String escapeRawText(String rawText) {
        return escapeFormatting(rawText);
    }

    /**
     * Checks whether a string is a six-digit hex color.
     *
     * @param value Value to check.
     * @return True when valid.
     */
    private static boolean isHexColor(String value) {
        if (value.length() != 6) {
            return false;
        }

        for (int index = 0; index < value.length(); index++) {
            if (Character.digit(value.charAt(index), 16) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a reverse color lookup table.
     *
     * @return Color-to-code lookup.
     */
    private static Map<String, Character> createColorCodes() {
        Map<String, Character> colorCodes = new HashMap<>();
        for (Map.Entry<Character, String> entry : MINECRAFT_COLORS.entrySet()) {
            colorCodes.put(entry.getValue(), entry.getKey());
        }
        return colorCodes;
    }

    /**
     * Mutable formatting state used while parsing text.
     */
    private static class FormattingState {

        private String color;
        private boolean bold;
        private boolean italic;
        private boolean monospace;

        /**
         * Resets all formatting.
         */
        private void reset() {
            color = null;
            bold = false;
            italic = false;
            monospace = false;
        }

        /**
         * Copies this state.
         *
         * @return State copy.
         */
        private FormattingState copy() {
            FormattingState copy = new FormattingState();
            copy.color = color;
            copy.bold = bold;
            copy.italic = italic;
            copy.monospace = monospace;
            return copy;
        }

        /**
         * Checks whether the default formatting is active.
         *
         * @return True when no formatting is active.
         */
        private boolean isDefault() {
            return color == null && !bold && !italic && !monospace;
        }

        /**
         * Checks whether a color is active.
         *
         * @return True when a color is active.
         */
        private boolean hasColor() {
            return color != null;
        }

        /**
         * Checks whether this state has a style that the next state disables.
         *
         * @param nextState Next state.
         * @return True when reset is needed.
         */
        private boolean hasEnabledStyleNotIn(FormattingState nextState) {
            return bold && !nextState.bold
                    || italic && !nextState.italic
                    || monospace && !nextState.monospace;
        }
    }
}
