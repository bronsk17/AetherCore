package me.bronsk.aethercore.ui.features.lang;

import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.lang.models.AELangMessage;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEColoredText;
import me.bronsk.aethercore.ui.components.AEConfirmPopup;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AEMultilineTextInput;
import me.bronsk.aethercore.ui.components.AEReadOnlyLabel;
import me.bronsk.aethercore.ui.components.AETextInput;
import me.bronsk.aethercore.utils.AEMessageUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/**
 * One lang message's detail view: code, description, and one translation input per installed
 * language - reached by clicking a row in {@link AEUILangView}, same "click -> navigate to a full
 * view, Delete|Save|Cancel footer" pattern as {@link
 * me.bronsk.aethercore.ui.features.permissions.AEUIGroupDetailsView}. The code is immutable once
 * created (Java code and other settings reference it as a literal string; renaming it would
 * silently break those). Delete (edit mode only) opens a confirm popup - a real destructive
 * confirmation still belongs in a popup, only the message's own editing surface moved to a view.
 */
public final class AEUILangMessageDetailsView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Lang/AEUILangMessageDetailsView.ui";

    private final boolean editMode;
    private String code;
    private String description;
    private final Map<String, String> translations;
    private String selectedLangCode;

    public static AEUILangMessageDetailsView createNew() {
        return new AEUILangMessageDetailsView(false, "", "", new HashMap<>());
    }

    public static AEUILangMessageDetailsView edit(AELangMessage message, Map<String, String> translations) {
        return new AEUILangMessageDetailsView(true, message.getCode(), message.getDescription(), new HashMap<>(translations));
    }

    private AEUILangMessageDetailsView(boolean editMode, String code, String description, Map<String, String> translations) {
        this.editMode = editMode;
        this.code = code;
        this.description = description == null ? "" : description;
        this.translations = translations;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("Title.Main", new AELabel(editMode ? lang("ui.lang.message.title_edit") : lang("ui.lang.message.title_new")));
        content.put("Details.CodeRow", getCodeRowComponent());
        content.put("Details.DescriptionInput", new AETextInput(lang("ui.lang.message.description_placeholder"), description, this::onDescriptionChanged));
        content.put("Details.Translations.LangSelector", getLangSelectorComponent());
        content.put("Details.Translations.MessageInput", getMessageInputComponent());
        content.put("Details.Translations.Preview", getPreviewComponent());
        content.put("Footer.DeleteButton", editMode
                ? List.of(new AEActionButton(lang("ui.common.delete"), true, this::confirmDelete))
                : List.of());
        content.put("Footer.SaveButton", new AEActionButton(lang("ui.common.save"), this::save));
        content.put("Footer.CancelButton", new AEActionButton(lang("ui.common.cancel"), this::cancel));
        return content;
    }

    private AEUIComponent getCodeRowComponent() {
        return editMode ? new AEReadOnlyLabel(code) : new AETextInput(lang("ui.lang.message.code_placeholder"), code, this::onCodeChanged);
    }

    private AEUIComponent getLangSelectorComponent() {
        return new AELangSelector(getLangModule().getLangCodes(), getSelectedLangCode(), this::onLangSelected);
    }

    private AEUIComponent getMessageInputComponent() {
        String value = translations.getOrDefault(getSelectedLangCode(), "");
        return new AEMultilineTextInput(lang("ui.lang.message.translation_placeholder"), value, this::onTranslationChanged);
    }

    /**
     * Colors resolved, but placeholders (e.g. {@code {player}}) left as literal text - unlike the
     * Chat module's settings preview, Lang has no idea which placeholders will apply to a given
     * message (it might become a chat format, a join message, a command response, ...), so there's
     * nothing sensible to substitute them with here.
     */
    private AEUIComponent getPreviewComponent() {
        String value = translations.getOrDefault(getSelectedLangCode(), "");
        return new AEColoredText(AEMessageUtils.fromColoredString(value));
    }

    private String getSelectedLangCode() {
        if (selectedLangCode == null) {
            List<String> langCodes = getLangModule().getLangCodes();
            selectedLangCode = langCodes.isEmpty() ? "" : langCodes.get(0);
        }
        return selectedLangCode;
    }

    private void onLangSelected(String langCode) {
        selectedLangCode = langCode;
        refresh("Details.Translations.LangSelector", "Details.Translations.MessageInput", "Details.Translations.Preview");
    }

    private void onTranslationChanged(String value) {
        translations.put(getSelectedLangCode(), value);
        refresh("Details.Translations.Preview");
    }

    private void onCodeChanged(String value) {
        code = value;
    }

    private void onDescriptionChanged(String value) {
        description = value;
    }

    private void cancel() {
        goBack();
    }

    private void save() {
        AEExecutionContext context = getActorContext();
        AEActionResult result = editMode
                ? getLangModule().handleUpdateLangMessage(context, code, description, translations)
                : getLangModule().handleCreateLangMessage(context, code, description, translations);
        showNotification(result);
        if (result.success()) {
            goBack();
        }
    }

    private void confirmDelete() {
        showPopup(new AEConfirmPopup(
                lang("ui.lang.message.confirm_delete.title", args("code", code)),
                lang("ui.lang.message.confirm_delete.message"),
                this::delete
        ));
    }

    private void delete() {
        showNotification(getLangModule().handleDeleteLangMessage(getActorContext(), code));
        goBack();
    }

    private AELangModule getLangModule() {
        return AEModulesManager.get().getModule(AELangModule.class);
    }
}
