package net.frozenorb.foxtrot.koth.events;

import lombok.Getter;
import net.frozenorb.foxtrot.koth.KOTH;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by macguy8 on 10/31/2014.
 */
public class KOTHActivatedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private KOTH koth;

    public KOTHActivatedEvent(KOTH koth) {
        this.koth = koth;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}