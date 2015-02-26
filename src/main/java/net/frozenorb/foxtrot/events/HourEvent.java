package net.frozenorb.foxtrot.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HourEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter private int hour;

    public HourEvent(int hour) {
        this.hour = hour;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}