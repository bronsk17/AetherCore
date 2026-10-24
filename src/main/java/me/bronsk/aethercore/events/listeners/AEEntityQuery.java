package me.bronsk.aethercore.events.listeners;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Matches any archetype backing a game entity, without depending on the deprecated
 * {@code AllLegacyEntityTypesQuery}.
 */
final class AEEntityQuery implements Query<EntityStore> {

    static final AEEntityQuery INSTANCE = new AEEntityQuery();

    private AEEntityQuery() {
    }

    @Override
    public boolean test(Archetype<EntityStore> archetype) {
        return EntityUtils.hasEntity(archetype);
    }

    @Override
    public boolean requiresComponentType(ComponentType<EntityStore, ?> componentType) {
        return false;
    }

    @Override
    public void validateRegistry(ComponentRegistry<EntityStore> registry) {
    }

    @Override
    public void validate() {
    }
}
