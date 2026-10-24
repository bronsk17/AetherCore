package me.bronsk.aethercore.modules.player;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.camera.SetFlyCameraMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.gamemode.GameModeTypes;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.math.vector.Rotation3f;
import me.bronsk.aethercore.AELogger;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.player.components.AEPlayerFreezeComponent;
import me.bronsk.aethercore.modules.player.components.AEPlayerVanishedComponent;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class AEGamePlayer {
    private final Ref<EntityStore> entityStoreRef;

    /**
     * Handles fromUuid.
     *
     * @param uuid Method argument.
     * @return Method result.
     */
    public static AEGamePlayer fromUuid(@Nonnull String uuid) {
        return fromUuid(UUID.fromString(uuid));
    }

    /**
     * Handles fromUuid.
     *
     * @param uuid Method argument.
     * @return Method result.
     */
    public static AEGamePlayer fromUuid(@Nonnull UUID uuid) {
        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        if (playerRef == null) { return null;}
        Ref<EntityStore> entityStoreRef = playerRef.getReference();
        if (entityStoreRef == null) { return null; }
        return fromEntityStoreRef(entityStoreRef);
    }

    @Nonnull
    public static AEGamePlayer fromEntityStoreRef(@Nonnull Ref<EntityStore> entityStoreRef) {
        return new AEGamePlayer(entityStoreRef);
    }

    /**
     * Creates a new instance.
     *
     * @param entityStoreRef Method argument.
     */
    private AEGamePlayer(@Nonnull Ref<EntityStore> entityStoreRef) {
        this.entityStoreRef = Objects.requireNonNull(entityStoreRef, "entityStoreRef");
    }

    /**
     * Returns getUuid.
     *
     * @return Method result.
     */
    @Nonnull
    public UUID getUuid() {
        return supplyOnWorld(() -> getPlayerRef().getUuid());
    }

    /**
     * Returns getPlayer.
     *
     * @return Method result.
     */
    @Nonnull
    public Player getPlayer() {
        final Player toReturn = getComponent(Player.getComponentType());
        if (toReturn == null) {
            throw new IllegalStateException("Missing Player component for entity " + entityStoreRef);
        }
        return toReturn;
    }

    /**
     * Returns getPlayerRef.
     *
     * @return Method result.
     */
    @Nonnull
    public PlayerRef getPlayerRef() {
        final PlayerRef toReturn = getComponent(PlayerRef.getComponentType());
        if (toReturn == null) {
            throw new IllegalStateException("Missing PlayerRef component for entity " + entityStoreRef);
        }
        return toReturn;
    }


        /**
     * Returns getStore.
     *
     * @return Method result.
     */
    @Nonnull
    public Store<EntityStore> getStore() {
        return entityStoreRef.getStore();
    }

    /**
     * Returns getEntityStoreRef.
     *
     * @return Method result.
     */
    @Nonnull
    public Ref<EntityStore> getEntityStoreRef() {
        return entityStoreRef;
    }

    public <T extends Component<EntityStore>> T getComponent(ComponentType<EntityStore, T> componentType) {
        return supplyOnWorld(() -> getStore().getComponent(entityStoreRef, componentType));
    }

    /**
     * Returns getPosition.
     *
     * @return Method result.
     */
    @Nonnull
    public Vector3d getPosition() {
        return new Vector3d(getTransform().getPosition());
    }

    /**
     * Returns the player's current transform.
     *
     * @return Current transform copy.
     */
    @Nonnull
    public com.hypixel.hytale.math.vector.Transform getTransform() {
        TransformComponent transform = getComponent(TransformComponent.getComponentType());
        if (transform == null) {
            throw new IllegalStateException("Missing TransformComponent for player " + getUuid());
        }
        return new com.hypixel.hytale.math.vector.Transform(transform.getTransform());
    }

    public Vector3i getBlockIsStandingOn() {
        return supplyOnWorld(() -> {
            Vector3d position = getPosition();
            return new Vector3i((int) Math.floor(position.x), (int) Math.floor(position.y - 0.001d), (int) Math.floor(position.z));
        });
    }

    /**
     * Returns the player's current username.
     *
     * @return Method result.
     */
    @Nonnull
    public String getUsername() {
        return supplyOnWorld(() -> getPlayerRef().getUsername());
    }

    /**
     * Returns the player's current remote IP address, or null if it can't be resolved.
     *
     * @return Method result.
     */
    public String getIp() {
        return supplyOnWorld(() -> {
            SocketAddress remoteAddress = getPlayerRef().getPacketHandler().getChannel().remoteAddress();
            if (remoteAddress instanceof InetSocketAddress inetAddress) {
                InetAddress address = inetAddress.getAddress();
                return address != null ? address.getHostAddress() : inetAddress.getHostString();
            }
            return remoteAddress == null ? null : remoteAddress.toString();
        });
    }

    public void kick(Message reason) {
        executeOnWorld(() -> getPlayerRef().getPacketHandler().disconnect(reason));
    }

    public void teleport(World targetWorld, com.hypixel.hytale.math.vector.Transform transform) {
        if (targetWorld == null || transform == null) {
            return;
        }

        Vector3d position = transform.getPosition();
        Rotation3f rotation = transform.getRotation();
        teleport(targetWorld, position.x, position.y, position.z, rotation.yaw(), rotation.pitch());
    }

    public void teleport(World targetWorld, double x, double y, double z) {
        teleport(targetWorld, x, y, z, 0f, 0f);
    }

    public void teleport(World targetWorld, double x, double y, double z, float yaw, float pitch) {
        if (targetWorld == null || !entityStoreRef.isValid()) {
            return;
        }

        executeOnWorld(() -> {
            Vector3d position = new Vector3d(x, y, z);
            Rotation3f rotation = new Rotation3f(pitch, yaw, 0);
            Teleport teleport = new Teleport(targetWorld, position, rotation);
            getStore().addComponent(entityStoreRef, Teleport.getComponentType(), teleport);
        });
    }

    /**
     * Handles sendMessage.
     *
     * @param message Method argument.
     */
    public void sendMessage(Message message) {
        executeOnWorld(() -> getPlayerRef().sendMessage(message));
    }

    /**
     * Check whether the player has a permission
     * @param perm The permission node to check
     * @return true if the player has the perm node, false otherwise
     */
    public boolean hasPermission(String perm) {
        return supplyOnWorld(() -> getPlayerRef().hasPermission(perm, false));
    }

    public World getWorld() {
        return getStore().getExternalData().getWorld();
    }

    public boolean isInWorldThread() {
        return getWorld().isInThread();
    }

    public CompletableFuture<Void> executeOnWorld(Runnable runnable) {
        if (isInWorldThread()) {
            runnable.run();
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(runnable, getWorld());
    }

    public <T> T supplyOnWorld(Supplier<T> supplier) {
        if (isInWorldThread()) {
            return supplier.get();
        }

        try {
            return supplyOnWorldAsync(supplier).get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while executing player operation on world thread", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Unable to execute player operation on world thread", exception);
        }
    }

    public <T> CompletableFuture<T> supplyOnWorldAsync(Supplier<T> supplier) {
        if (isInWorldThread()) {
            return CompletableFuture.completedFuture(supplier.get());
        }
        return CompletableFuture.supplyAsync(supplier, getWorld());
    }

    public void freeze() {
        executeOnWorld(() -> {
            if (!entityStoreRef.isValid()) {
                return;
            }
            Store<EntityStore> store = getStore();
            if (getComponent(AEPlayerFreezeComponent.getComponentType()) != null) {
                return;
            }
            AEPlayerFreezeComponent freezeComponent = new AEPlayerFreezeComponent();
            store.putComponent(entityStoreRef, AEPlayerFreezeComponent.getComponentType(), freezeComponent);
        });
    }

    public void unfreeze() {
        executeOnWorld(() -> {
            if (!entityStoreRef.isValid()) {
                return;
            }
            Store<EntityStore> store = getStore();
            store.removeComponentIfExists(entityStoreRef, AEPlayerFreezeComponent.getComponentType());
        });
    }

    public boolean isFrozen() {
        return getComponent(AEPlayerFreezeComponent.getComponentType()) != null;
    }

    /** Whether this player is currently vanished - see {@link #setIsVanished}. */
    public boolean getIsVanished() {
        return getComponent(AEPlayerVanishedComponent.getComponentType()) != null;
    }

    /**
     * Hides/reshows this player to every other online player across every world, via {@code
     * HiddenPlayersManager.hidePlayer}/{@code showPlayer} on each observer. Idempotent - a no-op if
     * already in the requested state.
     *
     * <p>Deliberately does NOT construct/write {@link RemoveFromServerPlayerList}/{@link
     * AddToServerPlayerList} packets by hand (an earlier version of this method did) -
     * {@code HiddenPlayersManager.hidePlayer}/{@code showPlayer} already send those internally, via
     * {@code ServerPlayerListModule.onPlayerHiddenForViewer}/{@code onPlayerShownForViewer}
     * (confirmed by decompiling the shipped SDK), with a correctly-resolved full player entry (spectating
     * state, lives, ping) for the re-add - a hand-built packet with placeholder {@code 0, false, null}
     * fields raced the real one and could leave an observer's tab list showing a broken entry for the
     * un-vanished player. The actual per-observer entity-visibility exclusion (not just the tab list)
     * is also driven by {@code hidePlayer} - the native {@code EntityTrackerSystems$HideFromPlayer}
     * tick system prunes any entity an observer's own {@code HiddenPlayersManager} reports hidden out
     * of that observer's visible-entity set every tick, which is what actually stops the vanished
     * player's model from being sent to/rendered by everyone else.
     */
    public void setIsVanished(boolean vanished) {
        executeOnWorld(() -> {
            if (!entityStoreRef.isValid() || vanished == getIsVanished()) {
                AELogger.logInfo("[vanish] setIsVanished(%s) no-op for %s (valid=%s, already=%s)",
                        vanished, getUuid(), entityStoreRef.isValid(), getIsVanished());
                return;
            }
            Store<EntityStore> store = getStore();
            if (vanished) {
                store.putComponent(entityStoreRef, AEPlayerVanishedComponent.getComponentType(), new AEPlayerVanishedComponent());
            } else {
                store.removeComponentIfExists(entityStoreRef, AEPlayerVanishedComponent.getComponentType());
            }

            UUID uuid = getUuid();
            if (vanished) {
                AEVanishRegistry.add(uuid);
            } else {
                AEVanishRegistry.remove(uuid);
            }

            int worldCount = Universe.get().getWorlds().size();
            AELogger.logInfo("[vanish] applying vanished=%s for %s across %d world(s)", vanished, uuid, worldCount);
            for (World iterWorld : Universe.get().getWorlds().values()) {
                iterWorld.execute(() -> applyVanishInWorld(iterWorld, uuid, vanished));
            }
        });
    }

    private static void applyVanishInWorld(World world, UUID vanishedUuid, boolean vanished) {
        try {
            int affected = 0;
            for (PlayerRef target : world.getPlayerRefs()) {
                if (target.getUuid().equals(vanishedUuid)) {
                    continue;
                }
                affected++;
                if (vanished) {
                    target.getHiddenPlayersManager().hidePlayer(vanishedUuid);
                } else {
                    target.getHiddenPlayersManager().showPlayer(vanishedUuid);
                }

                Ref<EntityStore> targetRef = target.getReference();
                if (targetRef == null) {
                    continue;
                }
                Player targetPlayer = world.getEntityStore().getStore().getComponent(targetRef, Player.getComponentType());
                if (targetPlayer != null) {
                    targetPlayer.getWorldMapTracker().setPlayerMapFilter(who -> !AEVanishRegistry.contains(who.getUuid()));
                }
            }
            AELogger.logInfo("[vanish] world '%s': applied vanished=%s for %s to %d observer(s)",
                    world.getName(), vanished, vanishedUuid, affected);
        } catch (Exception exception) {
            AELogger.logException("[vanish] applyVanishInWorld failed for world '" + world.getName() + "'", exception);
        }
    }

    /** "Adventure" or "Creative" - the two gamemodes the native SDK's own {@code GameMode} enum supports. Spectator is a different, richer mechanism - see {@link #enterSpectator}. */
    public void setGamemode(String gamemode) {
        executeOnWorld(() -> {
            if (!entityStoreRef.isValid()) {
                return;
            }
            Player.setGameMode(entityStoreRef, GameMode.valueOf(gamemode), getStore());
        });
    }

    public String getGamemode() {
        return getPlayer().getGameMode().name();
    }

    /**
     * Enters the native "Spectator" {@code GameModeType} asset - already gives flying, no-clip,
     * invulnerability and intangibility (invisible to collisions) as part of that asset's own
     * definition, nothing to add on our side.
     */
    public boolean enterSpectator() {
        return supplyOnWorld(() -> entityStoreRef.isValid() && GameModeTypes.enter(entityStoreRef, getStore(), "Spectator"));
    }

    public boolean exitSpectator() {
        return supplyOnWorld(() -> entityStoreRef.isValid() && GameModeTypes.exit(entityStoreRef, getStore()));
    }

    public boolean isSpectator() {
        return "Spectator".equals(GameModeTypes.getCurrentTypeId(entityStoreRef, getStore()));
    }

    /**
     * Grants/revokes the native fly-camera + no-clip permission nodes.
     *
     * <p>An earlier version of this method ALSO pushed {@link SetFlyCameraMode} directly to the
     * player's client (mirroring what {@code FlyCameraModule.checkAndEnforceFlyCameraPermission}
     * sends server-side to force-exit the mode on permission loss) to make the toggle immediately
     * observable, since decompiling the SDK shows nothing server-side proactively enters the mode on
     * a permission GRANT the way it does on a revoke - only the client itself, via some keybind/menu
     * sending {@code RequestFlyCameraMode}, normally triggers entry. That direct push is REMOVED:
     * correlating this plugin's own timestamped logs against the client's crash log confirmed the
     * client crashed (native {@code IndexOutOfRangeException}, no managed stack trace available)
     * about a second after two rapid {@code SetFlyCameraMode} pushes (an off-then-on toggle), while
     * an earlier single push at join time hadn't crashed anything for the following 20+ seconds - an
     * unprompted server-sent {@code SetFlyCameraMode} (one the client never asked for, or two in
     * quick succession) appears to hit a genuine client-side bug. Until a safe way to trigger it is
     * found, granting the permission is as far as this goes - the admin needs the base game's own
     * client-side control (if one exists) to actually enter fly-camera mode once they have it;
     * {@code hytale.movement.noclip} is unaffected by this - it's a continuous per-tick allowance the
     * server's own movement validation checks live against {@code AEPermissionsProvider}, no client
     * mode switch involved.
     */
    public void setFreeCam(boolean freeCam) {
        UUID uuid = getUuid();
        AEPermissionsModule permissions = AEModulesManager.get().getModule(AEPermissionsModule.class);
        Set<String> nodes = Set.of(
                com.hypixel.hytale.server.core.permissions.HytalePermissions.FLY_CAM.getId(),
                com.hypixel.hytale.server.core.permissions.HytalePermissions.NO_CLIP.getId());
        if (freeCam) {
            permissions.getUsersModule().addUserPermissions(uuid, nodes);
        } else {
            permissions.getUsersModule().removeUserPermissions(uuid, nodes);
        }
    }

    private static final float DEFAULT_BASE_SPEED = 5.5f;
    private static final float DEFAULT_HORIZONTAL_FLY_SPEED = 10.32f;
    private static final float DEFAULT_VERTICAL_FLY_SPEED = 10.32f;

    /** {@code level} 1 = normal walking speed (the default {@code Server/Entity/MovementConfig/Default.json} BaseSpeed), higher multiplies it - same {@code MovementSettings.baseSpeed} field {@code AEPlayerFreezeSystem} already zeroes out to freeze movement. Does not affect flying speed - see {@link #setFlySpeedLevel}. */
    public void setSpeedLevel(int level) {
        executeOnWorld(() -> {
            if (!entityStoreRef.isValid()) {
                return;
            }
            MovementManager movementManager = getComponent(MovementManager.getComponentType());
            if (movementManager == null || movementManager.getSettings() == null) {
                return;
            }
            movementManager.getSettings().baseSpeed = DEFAULT_BASE_SPEED * Math.max(1, level);
            PlayerRef playerRef = getComponent(PlayerRef.getComponentType());
            if (playerRef != null) {
                movementManager.update(playerRef.getPacketHandler());
            }
        });
    }

    /**
     * {@code level} 1 = normal flying speed (the default {@code Server/Entity/MovementConfig/Default.json}
     * HorizontalFlySpeed/VerticalFlySpeed, both 10.32), higher multiplies it - a genuinely separate
     * pair of {@code MovementSettings} fields from walking's {@code baseSpeed} (see {@link
     * #setSpeedLevel}), which is why FreeCam needs its own speed control rather than sharing one.
     */
    public void setFlySpeedLevel(int level) {
        executeOnWorld(() -> {
            if (!entityStoreRef.isValid()) {
                return;
            }
            MovementManager movementManager = getComponent(MovementManager.getComponentType());
            if (movementManager == null || movementManager.getSettings() == null) {
                return;
            }
            float multiplier = Math.max(1, level);
            movementManager.getSettings().horizontalFlySpeed = DEFAULT_HORIZONTAL_FLY_SPEED * multiplier;
            movementManager.getSettings().verticalFlySpeed = DEFAULT_VERTICAL_FLY_SPEED * multiplier;
            PlayerRef playerRef = getComponent(PlayerRef.getComponentType());
            if (playerRef != null) {
                movementManager.update(playerRef.getPacketHandler());
            }
        });
    }

}
