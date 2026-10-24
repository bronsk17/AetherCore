package me.bronsk.aethercore.ui.framework;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.ui.AEAdminPage;

/** Owns opening of the AetherCore native Custom UI page - see {@code AERootCommand} for the {@code /aether} command that triggers it. */
public final class AEUIManager {

    public void openMain(PlayerRef playerRef, Store<EntityStore> store) {
        if (!playerRef.isValid() || playerRef.getReference() == null) {
            return;
        }
        Player player = store.getComponent(playerRef.getReference(), Player.getComponentType());
        if (player == null) {
            return;
        }
        player.getPageManager().openCustomPage(playerRef.getReference(), store, new AEAdminPage(playerRef));
    }
}
