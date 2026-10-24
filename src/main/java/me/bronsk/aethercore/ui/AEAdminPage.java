package me.bronsk.aethercore.ui;

import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.ui.components.AENotificationBanner;
import me.bronsk.aethercore.ui.components.AEUINavButtons;
import me.bronsk.aethercore.ui.components.AEUITabs;
import me.bronsk.aethercore.ui.features.admintools.AEUIAdminToolsView;
import me.bronsk.aethercore.ui.features.chat.AEUIChatGroupSettingsView;
import me.bronsk.aethercore.ui.features.chat.AEUIChatPlayerSettingsView;
import me.bronsk.aethercore.ui.features.chat.AEUIChatView;
import me.bronsk.aethercore.ui.features.lang.AEUILangLangDetailsView;
import me.bronsk.aethercore.ui.features.lang.AEUILangMessageDetailsView;
import me.bronsk.aethercore.ui.features.lang.AEUILangView;
import me.bronsk.aethercore.ui.features.overview.AEUIOverviewView;
import me.bronsk.aethercore.ui.features.permissions.AEUIGroupDetailsView;
import me.bronsk.aethercore.ui.features.permissions.AEUIPermissionsView;
import me.bronsk.aethercore.ui.features.permissions.AEUIPlayerPermsDetailsView;
import me.bronsk.aethercore.ui.features.players.AEUIPlayerDetailsView;
import me.bronsk.aethercore.ui.features.players.AEUIPlayersView;
import me.bronsk.aethercore.ui.framework.AEUIHistory;
import me.bronsk.aethercore.ui.framework.AEUIPage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** The single native Custom UI page hosting every AetherCore admin tab. */
public final class AEAdminPage extends AEUIPage {

    private final PlayerRef playerRef;
    private final AEUIPlayersView playersView = new AEUIPlayersView();

    private final List<AEUITabDef> tabs = List.of(
            new AEUITabDef("ui.tab.overview", AEUIOverviewView.class, AEUIOverviewView::new, AEUIPerms.OVERVIEW),
            new AEUITabDef("ui.tab.players", AEUIPlayersView.class, () -> playersView, List.of(AEUIPlayerDetailsView.class), AEUIPerms.PLAYERS),
            new AEUITabDef("ui.tab.permissions", AEUIPermissionsView.class, AEUIPermissionsView::new,
                    List.of(AEUIGroupDetailsView.class, AEUIPlayerPermsDetailsView.class), AEUIPerms.PERMISSIONS),
            new AEUITabDef("ui.tab.chat", AEUIChatView.class, AEUIChatView::new,
                    List.of(AEUIChatGroupSettingsView.class, AEUIChatPlayerSettingsView.class), AEUIPerms.CHAT),
            new AEUITabDef("ui.tab.lang", AEUILangView.class, AEUILangView::new,
                    List.of(AEUILangMessageDetailsView.class, AEUILangLangDetailsView.class), AEUIPerms.LANG),
            new AEUITabDef("ui.tab.admintools", AEUIAdminToolsView.class, AEUIAdminToolsView::new, AEUIPerms.ADMIN_TOOLS)
    );

    private final AEUIHistory history;

    public AEAdminPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.playerRef = playerRef;
        history = enableHistory(new AEUIOverviewView());
    }

    public static AEAdminPage fromPlayerDetails(PlayerRef playerRef, UUID playerUUID) {
        AEAdminPage instance = new AEAdminPage(playerRef);
        AEPlayersModule tcPlayersModule = AEModulesManager.get().getModule(AEPlayersModule.class);
        instance.navigate(new AEUIPlayerDetailsView(tcPlayersModule.getPlayerByUuid(playerUUID)));
        return instance;
    }

    @Override
    protected String getRootTemplate() {
        return "Pages/AetherCore/Main.ui";
    }

    @Override
    protected Map<String, Object> getContent() {
        final List<AEUITabDef> tabsToShow = tabs.stream().filter(tab ->
                playerRef.hasPermission(tab.permission())).toList();
        return Map.of(
                "Sidebar.Panel.History", new AEUINavButtons(),
                "Sidebar.Panel.Tabs", new AEUITabs(tabsToShow, history.getCurrent().getClass()),
                "TabContent", history.getCurrent(),
                "Overlay", getPopup() == null ? List.of() : List.of(getPopup()),
                "Notification", getNotification() == null ? List.of() : List.of(new AENotificationBanner(getNotification()))
        );
    }
}
