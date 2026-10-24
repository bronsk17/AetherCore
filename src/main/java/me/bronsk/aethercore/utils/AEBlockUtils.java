package me.bronsk.aethercore.utils;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public final class AEBlockUtils {

    /**
     * Resolves the {@link BlockType} an item stack places, if any.
     *
     * @param itemStack Item stack held.
     * @return Placed block type, or null when the item isn't a placeable block.
     */
    public static BlockType getBlockTypeFromItemStack(ItemStack itemStack) {
        String blockKey = itemStack.getBlockKey();
        if (blockKey == null || blockKey.equals("Empty")) {
            return null;
        }
        return BlockType.getAssetMap().getAsset(blockKey);
    }

    private AEBlockUtils() {
    }
}
