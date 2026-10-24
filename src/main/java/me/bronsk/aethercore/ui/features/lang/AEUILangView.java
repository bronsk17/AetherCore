package me.bronsk.aethercore.ui.features.lang;

import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.AEUITabDef;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AEUITabs;

import java.util.List;
import java.util.Map;

/** The Lang tab: just the shell + sub-tab switch between Messages/Langs. */
public final class AEUILangView extends AEUIComponent {

    private static final String SHELL = "Pages/AetherCore/Components/Shells/SubTabsWithTitleShell.ui";

    private enum SubTab { MESSAGES, LANGS }

    private final AEUITabs subTabs;

    public AEUILangView() {
        this.subTabs = new AEUITabs(List.of(
                new AEUITabDef("ui.tab.lang.messages", SubTab.MESSAGES, AEUILangMessagesView::new),
                new AEUITabDef("ui.tab.lang.langs", SubTab.LANGS, AEUILangLangsView::new)
        ), SubTab.MESSAGES, false, this::onSubTabChanged);
    }

    @Override
    protected String getTemplate() {
        return SHELL;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Title.Main", new AELabel(lang("ui.lang.title")),
                "Title.Id", List.of(),
                "SubTabs", subTabs,
                "Details", subTabs.current()
        );
    }

    private void onSubTabChanged() {
        refresh("SubTabs", "Details");
    }
}
