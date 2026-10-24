package me.bronsk.aethercore.ui.features.lang;

import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.lang.models.AELang;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEButtonBar;
import me.bronsk.aethercore.ui.components.AETable;
import me.bronsk.aethercore.ui.components.AEUIColDef;

import java.util.List;
import java.util.Map;

/** Lang's Langs sub-tab: "+ Lang" above a table of every registered language, click a row to edit it. */
final class AEUILangLangsView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Lang/AEUILangLangsView.ui";

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Buttons", new AEButtonBar(List.of(new AEActionButton(lang("ui.lang.langs.add"), this::createNew))),
                "Table", new AETable<>("", getColumns(), lang("ui.lang.langs.empty"), this::fetch, this::openEdit, true)
        );
    }

    private List<AEUIColDef<AELang>> getColumns() {
        return List.of(
                new AEUIColDef<>(lang("ui.lang.col.code"), AELang::getCode),
                new AEUIColDef<>(lang("ui.lang.col.description"), entry -> entry.getDescription() == null || entry.getDescription().isBlank()
                        ? "-" : entry.getDescription())
        );
    }

    private AEPageResult<AELang> fetch(AEPageQuery query) {
        return getLangModule().getLangQueryResult(query);
    }

    private void createNew() {
        navigate(AEUILangLangDetailsView.createNew());
    }

    private void openEdit(AELang lang) {
        navigate(AEUILangLangDetailsView.edit(lang));
    }

    private AELangModule getLangModule() {
        return AEModulesManager.get().getModule(AELangModule.class);
    }
}
