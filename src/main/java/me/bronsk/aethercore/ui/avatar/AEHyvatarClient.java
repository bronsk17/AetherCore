package me.bronsk.aethercore.ui.avatar;

import me.bronsk.aethercore.AELogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/** Downloads a player's full-body rendered avatar from hyvatar.io. */
public final class AEHyvatarClient {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 'P', 'N', 'G'};

    /** Completes with the rendered PNG's bytes, or {@code null} if the render failed, the response wasn't a real PNG, or the request errored/timed out. Never completes exceptionally - callers only need to check for null. */
    public static CompletableFuture<byte[]> downloadFullRender(String username, int size) {
        String url = "https://hyvatar.io/render/full/" + encode(username) + "?size=" + size;
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(AEHyvatarClient::validatePng)
                .exceptionally(throwable -> {
                    AELogger.logWarning("Failed to download hyvatar.io avatar for %s: %s", username, throwable.getMessage());
                    return null;
                });
    }

    private static byte[] validatePng(HttpResponse<byte[]> response) {
        if (response.statusCode() != 200) {
            return null;
        }
        byte[] body = response.body();
        if (body == null || body.length < PNG_MAGIC.length) {
            return null;
        }
        for (int i = 0; i < PNG_MAGIC.length; i++) {
            if (body[i] != PNG_MAGIC[i]) {
                return null;
            }
        }
        return body;
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private AEHyvatarClient() {
    }
}
