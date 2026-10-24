package me.bronsk.aethercore.modules.player.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Objects;

public class AEPlayerFreezeComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, AEPlayerFreezeComponent> componentType;
    private Float previousBaseSpeed;

    public AEPlayerFreezeComponent() {
    }

    public static ComponentType<EntityStore, AEPlayerFreezeComponent> getComponentType() {
        if (componentType == null) {
            throw new IllegalStateException("AEPlayerFreezeComponent has not been registered");
        }
        return componentType;
    }

    public static void setComponentType(ComponentType<EntityStore, AEPlayerFreezeComponent> componentType) {
        if (AEPlayerFreezeComponent.componentType != null && AEPlayerFreezeComponent.componentType != componentType) {
            throw new IllegalStateException("AEPlayerFreezeComponent has already been registered");
        }
        AEPlayerFreezeComponent.componentType = Objects.requireNonNull(componentType, "componentType");
    }

    public Float getPreviousBaseSpeed() {
        return previousBaseSpeed;
    }

    public void setPreviousBaseSpeed(Float previousBaseSpeed) {
        this.previousBaseSpeed = previousBaseSpeed;
    }

    public void savePreviousSpeed(Float previousBaseSpeed) {
        if (this.previousBaseSpeed != null) {
            return;
        }
        setPreviousBaseSpeed(previousBaseSpeed);
    }

    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        AEPlayerFreezeComponent component = new AEPlayerFreezeComponent();
        component.previousBaseSpeed = previousBaseSpeed;
        return component;
    }

}
