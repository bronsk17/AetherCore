package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

/** A small colored status dot - green when online, red when offline. The color is a structural template choice, not reactive - reconstruct to change it. */
public final class AEStatusDot extends AEUIComponent {

    private static final String ONLINE = "Pages/AetherCore/Components/Status/AEStatusDotOnline.ui";
    private static final String OFFLINE = "Pages/AetherCore/Components/Status/AEStatusDotOffline.ui";

    private final boolean online;

    public AEStatusDot(boolean online) {
        this.online = online;
    }

    @Override
    protected String getTemplate() {
        return online ? ONLINE : OFFLINE;
    }
}
