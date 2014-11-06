package net.frozenorb.foxtrot.jedis.persist;

import net.frozenorb.foxtrot.jedis.RedisPersistMap;

/**
 * Created by macguy8 on 11/6/2014.
 */
public class FriendLivesMap extends RedisPersistMap<Integer> {

    public FriendLivesMap() {
        super("player_lives_friend");
    }

    @Override
    public String getRedisValue(Integer t) {
        return t + "";
    }

    @Override
    public Integer getJavaObject(String str) {
        return Integer.parseInt(str);
    }

    public int getLives(String name) {
        return getValue(name.toLowerCase()) != null ? getValue(name.toLowerCase()) : 0;
    }
}
