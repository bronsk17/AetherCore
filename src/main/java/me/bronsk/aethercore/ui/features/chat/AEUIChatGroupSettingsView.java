package me.bronsk.aethercore.ui.features.chat;

import com.hypixel.hytale.server.core.Message;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.chat.AEChatModule;
import me.bronsk.aethercore.modules.chat.AEChatSettingsModule;
import me.bronsk.aethercore.modules.chat.models.AEChatGroupSettings;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.lang.models.AELangMessage;
import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.text.AETextContext;
import me.bronsk.aethercore.text.AETextPlaceholders;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AECheckbox;
import me.bronsk.aethercore.ui.components.AEInputPreview;
import me.bronsk.aethercore.ui.components.AEInquiring;
import me.bronsk.aethercore.ui.components.AEInquiringField;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AETextInput;
import me.bronsk.aethercore.ui.components.AEUIColDef;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.utils.AEMessageUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/**
 * One group's chat settings, reached by clicking a row in {@link AEUIChatGroupsView} - the 6 {@code
 * /aether chat group} settings laid out as a 3x2 grid of {@link AEInputPreview} cells, one Save writing all
 * of them at once. The 4 message-code fields use the generic "inquiring" pattern ({@link
 * AEInquiringField} + {@link AEInquiring}) to pick an existing lang message instead of typing a code
 * by hand.
 */
public final class AEUIChatGroupSettingsView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Chat/AEUIChatSettingsView.ui";

    private static final String COLOR_ADDED = "#1f4f33";
    private static final String COLOR_REMOVED = "#4f2323";
    private static final String COLOR_MODIFIED = "#4f4023";

    private final String groupName;
    private String originalServerJoinMessageCode;
    private String originalPlayerJoinMessageCode;
    private String originalLeaveMessageCode;
    private String originalFirstJoinMessageCode;
    private String format;
    private String serverJoinMessageCode;
    private String playerJoinMessageCode;
    private String leaveMessageCode;
    private String firstJoinMessageCode;
    private boolean broadcastFirstJoinMessage;

    public AEUIChatGroupSettingsView(String groupName) {
        this.groupName = groupName;
        AEChatGroupSettings settings = getChatSettingsModule().getEffectiveGroupSettings(groupName);
        this.format = settings.getFormat();
        this.serverJoinMessageCode = settings.getServerJoinMessageCode();
        this.playerJoinMessageCode = settings.getPlayerJoinMessageCode();
        this.leaveMessageCode = settings.getLeaveMessageCode();
        this.firstJoinMessageCode = settings.getFirstJoinMessageCode();
        this.broadcastFirstJoinMessage = Boolean.TRUE.equals(settings.getBroadcastFirstJoinMessage());
        this.originalServerJoinMessageCode = serverJoinMessageCode;
        this.originalPlayerJoinMessageCode = playerJoinMessageCode;
        this.originalLeaveMessageCode = leaveMessageCode;
        this.originalFirstJoinMessageCode = firstJoinMessageCode;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("Title.Main", new AELabel(lang("ui.chat.group_settings.title", args("group", groupName))));
        content.put("Grid.Format", new AEInputPreview(lang("ui.chat.group_settings.format"),
                new AETextInput("{prefix} {player} {suffix}: {message}", format, this::onFormatChanged),
                previewFormat(format)));
        content.put("Grid.ServerJoin", new AEInputPreview(lang("ui.chat.group_settings.server_join"),
                new AEInquiringField(serverJoinMessageCode, this::openServerJoinPicker, true, this::onServerJoinCleared,
                        diffBackground(originalServerJoinMessageCode, serverJoinMessageCode)),
                previewJoinTemplate(serverJoinMessageCode)));
        content.put("Grid.PlayerJoin", new AEInputPreview(lang("ui.chat.group_settings.player_join"),
                new AEInquiringField(playerJoinMessageCode, this::openPlayerJoinPicker, true, this::onPlayerJoinCleared,
                        diffBackground(originalPlayerJoinMessageCode, playerJoinMessageCode)),
                previewJoinTemplate(playerJoinMessageCode)));
        content.put("Grid.Leave", new AEInputPreview(lang("ui.chat.group_settings.leave"),
                new AEInquiringField(leaveMessageCode, this::openLeavePicker, true, this::onLeaveCleared,
                        diffBackground(originalLeaveMessageCode, leaveMessageCode)),
                previewLeaveMessage(leaveMessageCode)));
        content.put("Grid.FirstJoin", new AEInputPreview(lang("ui.chat.group_settings.first_join"),
                new AEInquiringField(firstJoinMessageCode, this::openFirstJoinPicker, true, this::onFirstJoinCleared,
                        diffBackground(originalFirstJoinMessageCode, firstJoinMessageCode)),
                previewJoinTemplate(firstJoinMessageCode)));
        content.put("Grid.Broadcast", new AEInputPreview(lang("ui.chat.group_settings.broadcast_first_join"),
                new AECheckbox(broadcastFirstJoinMessage, this::onBroadcastChanged),
                previewBroadcast(broadcastFirstJoinMessage)));
        content.put("Footer.SaveButton", new AEActionButton(lang("ui.common.save"), this::save));
        content.put("Footer.CancelButton", new AEActionButton(lang("ui.common.cancel"), this::cancel));
        return content;
    }

    private List<AEUIColDef<AELangMessage>> getLangColumns() {
        return List.of(
                new AEUIColDef<>(lang("ui.lang.col.code"), AELangMessage::getCode),
                new AEUIColDef<>(lang("ui.lang.col.description"), message -> message.getDescription() == null || message.getDescription().isBlank()
                        ? "-" : message.getDescription())
        );
    }

    private void onFormatChanged(String value) {
        format = value;
        refresh("Grid.Format");
    }

    private void onBroadcastChanged(boolean value) {
        broadcastFirstJoinMessage = value;
        refresh("Grid.Broadcast");
    }

    private void openServerJoinPicker() {
        showPopup(newLangPicker(this::onServerJoinSelected));
    }

    private void openPlayerJoinPicker() {
        showPopup(newLangPicker(this::onPlayerJoinSelected));
    }

    private void openLeavePicker() {
        showPopup(newLangPicker(this::onLeaveSelected));
    }

    private void openFirstJoinPicker() {
        showPopup(newLangPicker(this::onFirstJoinSelected));
    }

    private AEInquiring<AELangMessage> newLangPicker(java.util.function.Consumer<AELangMessage> onSelected) {
        return new AEInquiring<>(lang("ui.lang.picker.title"), getLangColumns(), lang("ui.lang.picker.empty"), this::fetchLangMessages, AELangMessage::getCode, onSelected);
    }

    private void onServerJoinSelected(AELangMessage message) {
        serverJoinMessageCode = message.getCode();
        refresh("Grid.ServerJoin");
    }

    private void onPlayerJoinSelected(AELangMessage message) {
        playerJoinMessageCode = message.getCode();
        refresh("Grid.PlayerJoin");
    }

    private void onLeaveSelected(AELangMessage message) {
        leaveMessageCode = message.getCode();
        refresh("Grid.Leave");
    }

    private void onFirstJoinSelected(AELangMessage message) {
        firstJoinMessageCode = message.getCode();
        refresh("Grid.FirstJoin");
    }

    private void onServerJoinCleared() {
        serverJoinMessageCode = null;
        refresh("Grid.ServerJoin");
    }

    private void onPlayerJoinCleared() {
        playerJoinMessageCode = null;
        refresh("Grid.PlayerJoin");
    }

    private void onLeaveCleared() {
        leaveMessageCode = null;
        refresh("Grid.Leave");
    }

    private void onFirstJoinCleared() {
        firstJoinMessageCode = null;
        refresh("Grid.FirstJoin");
    }

    /** null (no tint) when unchanged, else green/added, red/removed, or yellow/modified against the value this view was opened with. */
    private String diffBackground(String original, String current) {
        boolean wasBlank = original == null || original.isBlank();
        boolean isBlank = current == null || current.isBlank();
        if (wasBlank && isBlank) {
            return null;
        }
        if (!wasBlank && !isBlank && original.equals(current)) {
            return null;
        }
        if (wasBlank) {
            return COLOR_ADDED;
        }
        if (isBlank) {
            return COLOR_REMOVED;
        }
        return COLOR_MODIFIED;
    }

    private AEPageResult<AELangMessage> fetchLangMessages(AEPageQuery query) {
        return getLangModule().getMessageQueryResult(query);
    }

    private void cancel() {
        goBack();
    }

    private void save() {
        AEChatGroupSettings form = new AEChatGroupSettings();
        form.setFormat(format);
        form.setServerJoinMessageCode(serverJoinMessageCode);
        form.setPlayerJoinMessageCode(playerJoinMessageCode);
        form.setLeaveMessageCode(leaveMessageCode);
        form.setFirstJoinMessageCode(firstJoinMessageCode);
        form.setBroadcastFirstJoinMessage(broadcastFirstJoinMessage);

        AEExecutionContext context = getActorContext();
        AEActionResult result = getChatSettingsModule().handleSaveGroupSettings(context, groupName, form);
        showNotification(result);
        if (result.success()) {
            originalServerJoinMessageCode = serverJoinMessageCode;
            originalPlayerJoinMessageCode = playerJoinMessageCode;
            originalLeaveMessageCode = leaveMessageCode;
            originalFirstJoinMessageCode = firstJoinMessageCode;
            refresh("Grid.ServerJoin", "Grid.PlayerJoin", "Grid.Leave", "Grid.FirstJoin");
        }
    }

    /**
     * Runs the SAME resolver pipeline production chat uses ({@link AETextPlaceholders#resolve}) - a
     * hand-rolled {@code .replace(...)} chain doesn't reproduce its "eat the adjacent space when a
     * placeholder resolves empty" behavior (e.g. no group suffix set), which made an earlier version
     * of this preview show an extra space that real chat never actually renders.
     */
    private Message previewFormat(String rawFormat) {
        AETextContext context = buildTextContext().withRawMessage(lang("ui.chat.preview_sample_message"));
        String resolved = AETextPlaceholders.get().resolve(rawFormat == null ? "" : rawFormat, context);
        return AEMessageUtils.fromColoredString(resolved.trim());
    }

    /** server/player/first-join messages - production resolves these through {@link AETextPlaceholders} too, with rawMessage set to the template itself (see {@link AEChatModule}). */
    private Message previewJoinTemplate(String code) {
        if (code == null || code.isBlank()) {
            return Message.raw(lang("ui.chat.no_message"));
        }
        String template = getLangModule().buildText(code, getActorContext());
        AETextContext context = buildTextContext().withRawMessage(template);
        return AEMessageUtils.fromColoredString(AETextPlaceholders.get().resolve(template, context));
    }

    /** The leave message never goes through {@link AETextPlaceholders} in production - just a direct {player}/{uuid} replace, no prefix/suffix (see {@link AEChatModule#getLeaveMessage}). */
    private Message previewLeaveMessage(String code) {
        if (code == null || code.isBlank()) {
            return Message.raw(lang("ui.chat.no_message"));
        }
        String template = getLangModule().buildText(code, getActorContext());
        AEExecutionContext execContext = getActorContext();
        String resolved = template
                .replace("{player}", execContext.actorName())
                .replace("{uuid}", String.valueOf(execContext.actorUuid()));
        return AEMessageUtils.fromColoredString(resolved);
    }

    private Message previewBroadcast(boolean broadcast) {
        return Message.raw(broadcast ? lang("ui.chat.broadcast_all") : lang("ui.chat.broadcast_joining_only"));
    }

    private AETextContext buildTextContext() {
        return AETextContext.fromPlayer(AEGamePlayer.fromUuid(getActorUuid()));
    }

    private AEChatSettingsModule getChatSettingsModule() {
        return AEModulesManager.get().getModule(AEChatModule.class).getSettingsModule();
    }

    private AELangModule getLangModule() {
        return AEModulesManager.get().getModule(AELangModule.class);
    }
}
