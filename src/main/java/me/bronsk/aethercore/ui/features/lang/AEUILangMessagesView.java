package me.bronsk.aethercore.ui.features.lang;

import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.lang.models.AELangMessage;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEButtonBar;
import me.bronsk.aethercore.ui.components.AETable;
import me.bronsk.aethercore.ui.components.AEUIColDef;

import java.util.List;
import java.util.Map;

/** Lang's Messages sub-tab: "+ Message" above a server-paginated table of every message code and its notes, click a row to edit it. */
final class AEUILangMessagesView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Lang/AEUILangMessagesView.ui";

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Buttons", new AEButtonBar(List.of(new AEActionButton(lang("ui.lang.messages.add"), this::createNew))),
                "Table", new AETable<>("", getColumns(), lang("ui.lang.messages.empty"), this::fetch, this::openEdit, true)
        );
    }

    private List<AEUIColDef<AELangMessage>> getColumns() {
        return List.of(
                new AEUIColDef<>(lang("ui.lang.col.code"), AELangMessage::getCode),
                new AEUIColDef<>(lang("ui.lang.messages.col.notes"), message -> message.getDescription() == null || message.getDescription().isBlank()
                        ? "-" : message.getDescription())
        );
    }

    private AEPageResult<AELangMessage> fetch(AEPageQuery query) {
        return getLangModule().getMessageQueryResult(query);
    }

    private void createNew() {
        navigate(AEUILangMessageDetailsView.createNew());
    }

    private void openEdit(AELangMessage message) {
        Map<String, String> translations = getLangModule().getTranslations(message.getCode());
        navigate(AEUILangMessageDetailsView.edit(message, translations));
    }

    private AELangModule getLangModule() {
        return AEModulesManager.get().getModule(AELangModule.class);
    }
}
