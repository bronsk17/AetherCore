package me.bronsk.aethercore.modules.player.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Objects;

/** Marker component for "this player is currently vanished" - presence alone is the whole state, mirrors {@code AEPlayerFreezeComponent}'s registration shape. */
public class AEPlayerVanishedComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, AEPlayerVanishedComponent> componentType;

    public AEPlayerVanishedComponent() {
    }

    public static ComponentType<EntityStore, AEPlayerVanishedComponent> getComponentType() {
        if (componentType == null) {
            throw new IllegalStateException("AEPlayerVanishedComponent has not been registered");
        }
        return componentType;
    }

    public static void setComponentType(ComponentType<EntityStore, AEPlayerVanishedComponent> componentType) {
        if (AEPlayerVanishedComponent.componentType != null && AEPlayerVanishedComponent.componentType != componentType) {
            throw new IllegalStateException("AEPlayerVanishedComponent has already been registered");
        }
        AEPlayerVanishedComponent.componentType = Objects.requireNonNull(componentType, "componentType");
    }

    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        return new AEPlayerVanishedComponent();
    }
}
