package de.ropemc.rope.event.events.client

import de.ropemc.rope.event.Cancelable
import de.ropemc.rope.event.Event

class ClientChatEvent extends Event implements Cancelable {
    String message

    ClientChatEvent(message) {
        this.message = message
    }
}
