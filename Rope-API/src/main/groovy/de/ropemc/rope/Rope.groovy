package de.ropemc.rope

import de.ropemc.rope.event.EventBus

interface Rope {

    static Rope rope

    String getVersion()

    EventBus getEventBus()
}
