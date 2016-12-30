package net.frozenorb.foxtrot.server;

import com.mongodb.BasicDBObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public enum Deathban {

    DEFAULT("", 240),
    VIP("inherit.vip", 120),
    PRO("inherit.pro", 90),
    EPIC("inherit.epic", 45),
    HIGHROLLER("inherit.highroller", 45);

    @Getter private final String permission;
    private int minutes;

    public int inSeconds() {
        return (int) TimeUnit.MINUTES.toSeconds(minutes); // hours -> seconds
    }

    private int toHours() {
        return (int) TimeUnit.MINUTES.toHours(minutes);
    }

    public static void load(BasicDBObject object) {
        for (String key : object.keySet()) {
            valueOf(key).minutes = object.getInt(key);
        }
    }

}
