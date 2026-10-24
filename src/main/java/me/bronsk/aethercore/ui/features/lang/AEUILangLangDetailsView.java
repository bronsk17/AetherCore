package me.bronsk.aethercore.ui.features.lang;

import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.lang.models.AELang;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEConfirmPopup;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AEReadOnlyLabel;
import me.bronsk.aethercore.ui.components.AETextInput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/**
 * One language's detail view: code and description - reached by clicking a row in {@link
 * AEUILangLangsView}, same "click -> navigate to a full view, Delete|Save|Cancel footer" pattern as
 * {@link AEUILangMessageDetailsView}. The code is immutable once created (translations and other
 * settings reference it as a literal string). Delete (edit mode only) opens a confirm popup.
 */
public final class AEUILangLangDetailsView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Lang/AEUILangLangDetailsView.ui";

    private final boolean editMode;
    private String code;
    private String description;

    public static AEUILangLangDetailsView createNew() {
        return new AEUILangLangDetailsView(false, "", "");
    }

    public static AEUILangLangDetailsView edit(AELang lang) {
        return new AEUILangLangDetailsView(true, lang.getCode(), lang.getDescription());
    }

    private AEUILangLangDetailsView(boolean editMode, String code, String description) {
        this.editMode = editMode;
        this.code = code;
        this.description = description == null ? "" : description;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("Title.Main", new AELabel(editMode ? lang("ui.lang.lang_details.title_edit") : lang("ui.lang.lang_details.title_new")));
        content.put("Details.CodeRow", getCodeRowComponent());
        content.put("Details.DescriptionInput", new AETextInput(lang("ui.lang.message.description_placeholder"), description, this::onDescriptionChanged));
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
                ? getLangModule().handleUpdateLang(context, code, description)
                : getLangModule().handleCreateLang(context, code, description);
        showNotification(result);
        if (result.success()) {
            goBack();
        }
    }

    private void confirmDelete() {
        showPopup(new AEConfirmPopup(
                lang("ui.lang.lang_details.confirm_delete.title", args("code", code)),
                lang("ui.lang.lang_details.confirm_delete.message"),
                this::delete
        ));
    }

    private void delete() {
        showNotification(getLangModule().handleDeleteLang(getActorContext(), code));
        goBack();
    }

    private AELangModule getLangModule() {
        return AEModulesManager.get().getModule(AELangModule.class);
    }
}
