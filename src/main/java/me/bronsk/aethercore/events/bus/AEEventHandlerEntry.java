package me.bronsk.aethercore.events.bus;

import java.lang.reflect.Method;

/**
 * Classe interna che rappresenta un metodo da chiamare quando arriva un evento
 */
class AEEventHandlerEntry {

    private final Object instance;   // null per metodi statici
    private final Method method;
    private final int priority;

    /**
     * Creates a new instance.
     *
     * @param instance Method argument.
     * @param method Method argument.
     * @param priority Method argument.
     */
    public AEEventHandlerEntry(Object instance, Method method, int priority) {
        this.instance = instance;
        this.method = method;
        this.priority = priority;
    }

    /**
     * Handles invoke.
     *
     * @param event Method argument.
     * @throws Exception when the operation fails.
     */
    public void invoke(Object event) throws Exception {
        if (instance == null) {
            method.invoke(null, event);        // static
        } else {
            method.invoke(instance, event);    // instance
        }
    }

    /**
     * Returns getPriority.
     *
     * @return Method result.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns getListenerInstance.
     *
     * @return Method result.
     */
    public Object getListenerInstance() {
        return instance;
    }

    /**
     * Handles toString.
     *
     * @return Method result.
     */
    @Override
    public String toString() {
        return "EventHandlerEntry{" +
                "instance=" + instance +
                ", method=" + method +
                ", priority=" + priority +
                '}';
    }
}
