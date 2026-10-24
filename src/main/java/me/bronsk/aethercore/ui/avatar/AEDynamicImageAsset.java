package me.bronsk.aethercore.ui.avatar;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.setup.AssetFinalize;
import com.hypixel.hytale.protocol.packets.setup.AssetInitialize;
import com.hypixel.hytale.protocol.packets.setup.AssetPart;
import com.hypixel.hytale.protocol.packets.setup.RequestCommonAssetsRebuild;
import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.io.PacketHandler;

import java.util.concurrent.CompletableFuture;

/**
 * A runtime-registered common asset - unlike everything else under the plugin's asset pack (loaded
 * once, from bundled files, at connect time), this is added to {@link CommonAssetRegistry} on
 * demand, after the server is already running, under a brand-new name that never collides with a
 * bundled placeholder (a previous attempt reused a fixed name that already had a real bundled file
 * behind it, registered with a made-up hash - that name/hash mismatch is what broke the connection,
 * not the push itself). Referenced from markup through the native {@code AssetImage} element's
 * {@code AssetPath} property (see the official Custom UI type documentation) - not through the
 * file-relative {@code UIPath}/{@code TexturePath} convention every bundled texture otherwise uses.
 *
 * <p>Registering alone isn't enough for an already-connected client to actually have the bytes -
 * {@link #pushTo} still needs to deliver them directly, same as the SDK's own common-asset sync
 * does at login, just aimed at one connection instead of broadcast at connect time.
 */
public final class AEDynamicImageAsset extends CommonAsset {

    private final byte[] data;

    private AEDynamicImageAsset(String name, byte[] data) {
        super(name, data);
        this.data = data;
    }

    /** Registers {@code data} under a brand-new, never-reused name (real content hash, no placeholder ever shared that name) and returns the asset - pass its {@link #getName()} to an {@code AssetImage}'s {@code AssetPath}, and the asset itself to {@link #pushTo}. */
    public static AEDynamicImageAsset register(String namePrefix, byte[] data) {
        String name = namePrefix + "-" + System.nanoTime() + ".png";
        AEDynamicImageAsset asset = new AEDynamicImageAsset(name, data);
        CommonAssetRegistry.addCommonAsset(name, asset);
        return asset;
    }

    /** Delivers this asset's bytes directly to one connection and asks it to rebuild - same packet sequence the SDK's own common-asset sync uses at login, sent ad hoc instead. */
    public void pushTo(PacketHandler handler) {
        byte[] allBytes = getBlob().join();
        byte[][] parts = ArrayUtil.split(allBytes, 2621440);

        ToClientPacket[] packets = new ToClientPacket[1 + parts.length];
        packets[0] = new AssetInitialize(toPacket(), allBytes.length);
        for (int partIndex = 0; partIndex < parts.length; partIndex++) {
            packets[1 + partIndex] = new AssetPart(parts[partIndex]);
        }
        handler.write(packets, new AssetFinalize());
        handler.writeNoCache(new RequestCommonAssetsRebuild());
    }

    @Override
    protected CompletableFuture<byte[]> getBlob0() {
        return CompletableFuture.completedFuture(data);
    }
}
