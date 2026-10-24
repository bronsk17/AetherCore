package me.bronsk.aethercore.events.bus;

import me.bronsk.aethercore.AELogger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event bus with priority and cancellable event support.
 */
public class AEEventBus {

    private static final AEEventBus INSTANCE = new AEEventBus();

    private final Map<Class<? extends AEBaseEvent>, List<AEEventHandlerEntry>> handlers = new ConcurrentHashMap<>();

    /**
     * Returns the shared event bus instance.
     *
     * @return Shared event bus.
     */
    public static AEEventBus get() {
        return INSTANCE;
    }

    /**
     * Creates a new instance.
     */
    private AEEventBus() {
    }

    /**
     * Handles register.
     *
     * @param listener Method argument.
     */
    public void register(Object listener) {
        registerInternal(listener.getClass(), listener);
    }

    /**
     * Handles register.
     *
     * @param listenerClass Method argument.
     */
    public void register(Class<?> listenerClass) {
        registerInternal(listenerClass, null);
    }

    /**
     * Handles registerInternal.
     *
     * @param clazz Method argument.
     * @param instance Method argument.
     */
    private void registerInternal(Class<?> clazz, Object instance) {
        for (Method method : clazz.getDeclaredMethods()) {
            AEEventHandler annotation = method.getAnnotation(AEEventHandler.class);
            if (annotation == null || method.getParameterCount() != 1) {
                continue;
            }

            Class<?> paramType = method.getParameterTypes()[0];
            if (!AEBaseEvent.class.isAssignableFrom(paramType)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<? extends AEBaseEvent> eventType = (Class<? extends AEBaseEvent>) paramType;

            method.setAccessible(true);
            AEEventHandlerEntry entry = new AEEventHandlerEntry(instance, method, annotation.priority());

            handlers.computeIfAbsent(eventType, key -> new ArrayList<>()).add(entry);
            handlers.get(eventType).sort(Comparator.comparingInt(AEEventHandlerEntry::getPriority));
        }
    }

    /**
     * Handles unregister.
     *
     * @param listener Method argument.
     */
    public void unregister(Object listener) {
        handlers.values().forEach(list ->
                list.removeIf(entry -> entry.getListenerInstance() == listener));
    }

    /**
     * Dispatches an event to its registered handlers in priority order, stopping early
     * if the event is cancellable and gets cancelled by an earlier handler.
     *
     * @param event Event to dispatch.
     */
    public void post(AEBaseEvent event) {
        List<AEEventHandlerEntry> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers == null || eventHandlers.isEmpty()) {
            return;
        }

        boolean cancellable = event instanceof AEBaseCancellableEvent;
        for (AEEventHandlerEntry handler : new ArrayList<>(eventHandlers)) {
            if (cancellable && ((AEBaseCancellableEvent) event).isCancelled()) {
                break;
            }

            try {
                handler.invoke(event);
            } catch (Exception e) {
                AELogger.logException("Error while handling event " + event.getClass().getSimpleName(), e);
            }
        }
    }
}
