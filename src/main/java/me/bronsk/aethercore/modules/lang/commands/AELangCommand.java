package me.bronsk.aethercore.modules.lang.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import me.bronsk.aethercore.commands.arguments.AELangArg;
import me.bronsk.aethercore.commands.arguments.AELangMessageCodeArg;
import me.bronsk.aethercore.commands.arguments.AEStrippedStringArg;
import me.bronsk.aethercore.commands.AECollectionCommand;
import me.bronsk.aethercore.commands.AECommand;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.lang.AELangPerms;

import java.util.Map;

public class AELangCommand extends AECollectionCommand {

    /**
     * Creates the language command and registers its subcommand groups.
     */
    public AELangCommand() {
        super("lang", "AetherCore language commands");
        addSubCommand(new AELangListCommand());
        addSubCommand(new AELangLangCommand());
        addSubCommand(new AELangMessageCommand());
    }

    private static class AELangListCommand extends AECommand {

        private AELangListCommand() {
            super("list", "Lists languages");
            requirePermission(AELangPerms.LIST);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            AELangModule tcLangModule = AEModulesManager.get().getModule(AELangModule.class);
            reply(commandContext, tcLangModule.handleListLangs(getExecutionContext(commandContext)));
        }
    }

    /** {@code /aether lang lang add|remove|desc} - manages installed languages. */
    private static class AELangLangCommand extends AECollectionCommand {

        private AELangLangCommand() {
            super("lang", "Manages installed languages");
            addSubCommand(new AELangLangAddCommand());
            addSubCommand(new AELangLangRemoveCommand());
            addSubCommand(new AELangLangDescCommand());
        }

        private static class AELangLangAddCommand extends AECommand {

            private final RequiredArg<String> langArg;
            private final RequiredArg<String> descriptionArg;

            private AELangLangAddCommand() {
                super("add", "Adds a language");
                requirePermission(AELangPerms.LANG_ADD);
                this.langArg = withRequiredArg("lang", "Language code", ArgTypes.STRING);
                this.descriptionArg = withRequiredArg("description", "Language description", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                String lang = langArg.get(commandContext);
                String description = descriptionArg.get(commandContext);
                AELangModule tcLangModule = AEModulesManager.get().getModule(AELangModule.class);
                reply(commandContext, tcLangModule.handleAddLang(getExecutionContext(commandContext), lang, description));
            }
        }

        private static class AELangLangRemoveCommand extends AECommand {

            private final RequiredArg<String> langArg;

            private AELangLangRemoveCommand() {
                super("remove", "Removes a language");
                requirePermission(AELangPerms.LANG_REMOVE);
                this.langArg = withRequiredArg("lang", "Language code", new AELangArg());
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                String lang = langArg.get(commandContext);
                AELangModule tcLangModule = AEModulesManager.get().getModule(AELangModule.class);
                reply(commandContext, tcLangModule.handleRemoveLang(getExecutionContext(commandContext), lang));
            }
        }

        private static class AELangLangDescCommand extends AECommand {

            private final RequiredArg<String> langArg;
            private final RequiredArg<String> descriptionArg;

            private AELangLangDescCommand() {
                super("desc", "Changes a language's description");
                requirePermission(AELangPerms.LANG_DESC);
                this.langArg = withRequiredArg("lang", "Language code", new AELangArg());
                this.descriptionArg = withRequiredArg("description", "Language description", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                String lang = langArg.get(commandContext);
                String description = descriptionArg.get(commandContext);
                AELangModule tcLangModule = AEModulesManager.get().getModule(AELangModule.class);
                reply(commandContext, tcLangModule.handleSetLangDescription(getExecutionContext(commandContext), lang, description));
            }
        }
    }

    /** {@code /aether lang message add|set|remove} - manages message codes and their translations. */
    private static class AELangMessageCommand extends AECollectionCommand {

        private AELangMessageCommand() {
            super("message", "Manages message codes and their translations");
            addSubCommand(new AELangMessageAddCommand());
            addSubCommand(new AELangMessageSetCommand());
            addSubCommand(new AELangMessageRemoveCommand());
            addSubCommand(new AELangMessageDescCommand());
        }

        /** Creates a message code with its translation in the plugin's default language. */
        private static class AELangMessageAddCommand extends AECommand {

            private final RequiredArg<String> codeArg;
            private final RequiredArg<String> translationArg;

            private AELangMessageAddCommand() {
                super("add", "Adds a message with its default-language translation");
                requirePermission(AELangPerms.MESSAGE_ADD);
                this.codeArg = withRequiredArg("code", "Message code", ArgTypes.STRING);
                this.translationArg = withRequiredArg("translation", "Translation text", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                String code = codeArg.get(commandContext);
                String translation = translationArg.get(commandContext);
                AELangModule tcLangModule = AEModulesManager.get().getModule(AELangModule.class);
                String pluginLang = AEModulesManager.get().getGlobalConfig().getPluginLang();
                reply(commandContext, tcLangModule.handleSetLangMessage(getExecutionContext(commandContext), pluginLang, code, translation));
            }
        }

        private static class AELangMessageSetCommand extends AECommand {

            private final RequiredArg<String> codeArg;
            private final RequiredArg<String> langArg;
            private final RequiredArg<String> translationArg;

            private AELangMessageSetCommand() {
                super("set", "Sets a message's translation for one language");
                requirePermission(AELangPerms.MESSAGE_SET);
                this.codeArg = withRequiredArg("code", "Message code", new AELangMessageCodeArg());
                this.langArg = withRequiredArg("lang", "Language code", new AELangArg());
                this.translationArg = withRequiredArg("translation", "Translation text", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                String code = codeArg.get(commandContext);
                String lang = langArg.get(commandContext);
                String translation = translationArg.get(commandContext);
                AELangModule tcLangModule = AEModulesManager.get().getModule(AELangModule.class);
                reply(commandContext, tcLangModule.handleSetLangMessage(getExecutionContext(commandContext), lang, code, translation));
            }
        }

        private static class AELangMessageRemoveCommand extends AECommand {

            private final RequiredArg<String> codeArg;

            private AELangMessageRemoveCommand() {
                super("remove", "Removes a message and every translation of it");
                requirePermission(AELangPerms.MESSAGE_REMOVE);
                this.codeArg = withRequiredArg("code", "Message code", new AELangMessageCodeArg());
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                String code = codeArg.get(commandContext);
                AELangModule tcLangModule = AEModulesManager.get().getModule(AELangModule.class);
                reply(commandContext, tcLangModule.handleDeleteLangMessage(getExecutionContext(commandContext), code).message());
            }
        }

        /** Changes a message's description, leaving its translations untouched - {@code handleUpdateLangMessage} only upserts entries actually present in the translations map, so an empty one touches none. */
        private static class AELangMessageDescCommand extends AECommand {

            private final RequiredArg<String> codeArg;
            private final RequiredArg<String> descriptionArg;

            private AELangMessageDescCommand() {
                super("desc", "Changes a message's description");
                requirePermission(AELangPerms.MESSAGE_DESC);
                this.codeArg = withRequiredArg("code", "Message code", new AELangMessageCodeArg());
                this.descriptionArg = withRequiredArg("description", "Message description", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                String code = codeArg.get(commandContext);
                String description = descriptionArg.get(commandContext);
                AELangModule tcLangModule = AEModulesManager.get().getModule(AELangModule.class);
                reply(commandContext, tcLangModule.handleUpdateLangMessage(getExecutionContext(commandContext), code, description, Map.of()).message());
            }
        }
    }
}
