package de.ropemc.rope.event

import java.util.function.Consumer

interface EventBus {
    public <T> void subscribe(Class<T> to, Consumer<T> with)

    def fire(Event event)
}