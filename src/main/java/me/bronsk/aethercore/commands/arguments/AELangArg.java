package me.bronsk.aethercore.commands.arguments;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.utils.AEMessageUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.List;

public class AELangArg extends SingleArgumentType<String> {

    public AELangArg() {
        super("lang", "Language code", "en-US", "it-IT");
    }

    @Override
    public void suggest(@NonNullDecl CommandSender sender, @NonNullDecl String textAlreadyEntered,
                        int numParametersTyped, @NonNullDecl SuggestionResult result) {
        AELangModule tcLangModule = AEModulesManager.get().getModule(AELangModule.class);
        String lowerTextAlreadyEntered = textAlreadyEntered.toLowerCase();
        for (String langCode : tcLangModule.getLangCodes()) {
            if (langCode.toLowerCase().startsWith(lowerTextAlreadyEntered)) {
                result.suggest(langCode);
            }
        }
    }

    @NullableDecl
    @Override
    public String parse(String value, ParseResult parseResult) {
        AELangModule tcLangModule = AEModulesManager.get().getModule(AELangModule.class);
        String langCode = tcLangModule.resolveLangCode(value);
        if (langCode != null) {
            return langCode;
        }

        List<String> langCodes = tcLangModule.getLangCodes();
        parseResult.fail(
                Message.empty()
                        .insert(AEMessageUtils.fromColoredString("&7Unknown language &6" + value))
                        .insert(Message.raw(""))
                        .insert(Message.translation("server.general.failed.didYouMean")
                                .param("choices", StringUtil.sortByFuzzyDistance(
                                        value.toLowerCase(), langCodes, CommandUtil.RECOMMEND_COUNT).toString())));
        return null;
    }
}
