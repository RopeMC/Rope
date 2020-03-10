package de.ropemc.rope.mc115.event

import de.ropemc.rope.event.Event
import de.ropemc.rope.event.EventBus

import java.util.function.Consumer

class SimpleEventBus implements EventBus {
    private Map<Class, List<Consumer>> subscriptions = [:]

    def <T> void subscribe(Class<T> to, Consumer<T> with) {
        if (!subscriptions.containsValue(to)) {
            subscriptions[to] = []
        }
        subscriptions[to].add(with)
    }

    def fire(Event event) {
        subscriptions[event.class].forEach(consumer -> consumer.accept(event))
    }
}
