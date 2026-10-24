package me.bronsk.aethercore.modules.lang.defaults;

import me.bronsk.aethercore.database.ITCDefaultInstaller;
import me.bronsk.aethercore.modules.lang.AELangModule;

import java.sql.SQLException;
import java.util.Objects;

/** Installs the language every shipped default message is seeded under. */
public final class AEDefaultLanguage implements ITCDefaultInstaller {

    public static final String CODE = "en-US";
    public static final String DESCRIPTION = "English (US)";

    private final AELangModule langModule;

    public AEDefaultLanguage(AELangModule langModule) {
        this.langModule = Objects.requireNonNull(langModule, "langModule");
    }

    @Override
    public void install() throws SQLException {
        langModule.installDefaultLang(CODE, DESCRIPTION);
    }
}
