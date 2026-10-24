package me.bronsk.aethercore.commands.arguments;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.utils.AEMessageUtils;
import me.bronsk.aethercore.modules.lang.AELangModule;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.List;

public class AELangMessageCodeArg extends SingleArgumentType<String> {

    public AELangMessageCodeArg() {
        super("message_code", "Message code", "chat.message.cancelled", "world.region.create.success");
    }

    @Override
    public void suggest(@NonNullDecl CommandSender sender, @NonNullDecl String textAlreadyEntered,
                        int numParametersTyped, @NonNullDecl SuggestionResult result) {
        AELangModule tcLangModule = AEModulesManager.get().getModule(AELangModule.class);
        String lowerTextAlreadyEntered = textAlreadyEntered.toLowerCase();
        for (String messageCode : tcLangModule.getMessageCodes()) {
            if (messageCode.toLowerCase().startsWith(lowerTextAlreadyEntered)) {
                result.suggest(messageCode);
            }
        }
    }

    @NullableDecl
    @Override
    public String parse(String value, ParseResult parseResult) {
        AELangModule tcLangModule = AEModulesManager.get().getModule(AELangModule.class);
        String messageCode = tcLangModule.resolveMessageCode(value);
        if (messageCode != null) {
            return messageCode;
        }

        List<String> messageCodes = tcLangModule.getMessageCodes();
        parseResult.fail(
                Message.empty()
                        .insert(AEMessageUtils.fromColoredString("&7Unknown message code &6" + value))
                        .insert(Message.raw(""))
                        .insert(Message.translation("server.general.failed.didYouMean")
                                .param("choices", StringUtil.sortByFuzzyDistance(
                                        value.toLowerCase(), messageCodes, CommandUtil.RECOMMEND_COUNT).toString())));
        return null;
    }
}
