package com.agri.common.pattern;

import com.agri.common.event.DomainEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Factory class for creating domain events using the Factory Design Pattern.
 * Implements the Open-Closed Principle by allowing new event types to be registered
 * without modifying existing code.
 * 
 * Thread-safe implementation using ConcurrentHashMap.
 */
public class EventFactory {
    
    private static final Map<String, Supplier<? extends DomainEvent>> eventRegistry = new ConcurrentHashMap<>();
    
    /**
     * Register a new event type with its supplier.
     * Allows adding new event types without modifying the factory code (Open-Closed Principle).
     * 
     * @param eventType The string identifier for the event type
     * @param supplier The supplier that creates instances of the event
     */
    public static void register(String eventType, Supplier<? extends DomainEvent> supplier) {
        eventRegistry.put(eventType, supplier);
    }
    
    /**
     * Create an event instance by type.
     * 
     * @param eventType The type of event to create
     * @return A new instance of the requested event type
     * @throws IllegalArgumentException if the event type is not registered
     */
    public static DomainEvent create(String eventType) {
        Supplier<? extends DomainEvent> supplier = eventRegistry.get(eventType);
        if (supplier == null) {
            throw new IllegalArgumentException("Event type not registered: " + eventType);
        }
        return supplier.get();
    }
    
    /**
     * Check if an event type is registered.
     * 
     * @param eventType The event type to check
     * @return true if the event type is registered, false otherwise
     */
    public static boolean supports(String eventType) {
        return eventRegistry.containsKey(eventType);
    }
    
    /**
     * Get all registered event types.
     * 
     * @return A map of all registered event types and their suppliers
     */
    public static Map<String, Supplier<? extends DomainEvent>> getRegisteredTypes() {
        return Map.copyOf(eventRegistry);
    }
}
