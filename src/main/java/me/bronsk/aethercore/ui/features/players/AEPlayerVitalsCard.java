package me.bronsk.aethercore.ui.features.players;

import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AEProgressBarValue;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/** Overview's Health/Stamina card - two labeled progress bars (red health, yellow stamina), read from the live {@code EntityStatMap} while the player is online. */
final class AEPlayerVitalsCard extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Players/AEPlayerVitalsCard.ui";

    private final String healthText;
    private final float healthPercent;
    private final String staminaText;
    private final float staminaPercent;

    AEPlayerVitalsCard(String healthText, float healthPercent, String staminaText, float staminaPercent) {
        this.healthText = healthText;
        this.healthPercent = healthPercent;
        this.staminaText = staminaText;
        this.staminaPercent = staminaPercent;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "HealthLabel", new AELabel(healthText),
                "HealthBar", new AEProgressBarValue(healthPercent),
                "StaminaLabel", new AELabel(staminaText),
                "StaminaBar", new AEProgressBarValue(staminaPercent)
        );
    }
}
