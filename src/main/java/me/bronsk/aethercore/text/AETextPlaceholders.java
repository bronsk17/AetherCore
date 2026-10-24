package me.bronsk.aethercore.text;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AETextPlaceholders {

    private static AETextPlaceholders instance;

    private final Map<String, AETextResolver> resolvers = new ConcurrentHashMap<>();

    private AETextPlaceholders() {
        registerDefaultResolvers();
    }

    public static AETextPlaceholders get() {
        if (instance == null) {
            instance = new AETextPlaceholders();
        }
        return instance;
    }

    public void register(String placeholder, AETextResolver resolver) {
        if (placeholder == null || resolver == null) {
            return;
        }

        String normalizedPlaceholder = normalize(placeholder);
        if (!normalizedPlaceholder.isBlank()) {
            resolvers.put(normalizedPlaceholder, resolver);
        }
    }

    public String resolve(String template, AETextContext context) {
        String source = template == null ? "" : template;
        StringBuilder resolved = new StringBuilder();
        int index = 0;
        while (index < source.length()) {
            int open = source.indexOf('{', index);
            if (open < 0) {
                resolved.append(source.substring(index));
                break;
            }
            int close = source.indexOf('}', open + 1);
            if (close < 0) {
                resolved.append(source.substring(index));
                break;
            }
            resolved.append(source, index, open);
            String placeholder = normalize(source.substring(open, close + 1));
            AETextResolver resolver = resolvers.get(placeholder);
            if (resolver == null) {
                resolved.append(source, open, close + 1);
            } else {
                String replacement = resolver.resolve(context);
                if (replacement == null || replacement.isEmpty()) {
                    index = removeAdjacentPlaceholderSpace(source, resolved, close + 1);
                    continue;
                }
                resolved.append(replacement);
            }
            index = close + 1;
        }
        return resolved.toString();
    }

    public void clear() {
        resolvers.clear();
        registerDefaultResolvers();
    }

    private void registerDefaultResolvers() {
        register("player", context -> context.player() == null ? "" : context.player().getPlayerRef().getUsername());
        register("uuid", context -> context.player() == null ? "" : context.player().getUuid().toString());
    }

    private String normalize(String placeholder) {
        String normalized = placeholder.trim();
        if (normalized.startsWith("{") && normalized.endsWith("}") && normalized.length() > 1) {
            return normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }

    private int removeAdjacentPlaceholderSpace(String source, StringBuilder resolved, int nextIndex) {
        if (!resolved.isEmpty() && Character.isWhitespace(resolved.charAt(resolved.length() - 1))) {
            resolved.deleteCharAt(resolved.length() - 1);
            return nextIndex;
        }

        if (nextIndex < source.length() && Character.isWhitespace(source.charAt(nextIndex))) {
            return nextIndex + 1;
        }

        return nextIndex;
    }
}
