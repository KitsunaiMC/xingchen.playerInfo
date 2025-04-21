package xingchen.xingchenPlayerInfo;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class lastSaveTime {
    public static final Map<String, Timestamp> lastSaveTime = new ConcurrentHashMap<>();

    public static Timestamp getLoginTime(String name) {
        return lastSaveTime.get(name);
    }

    public static void removeLoginTime(String name) {
        lastSaveTime.remove(name);
    }

    public static void setLoginTime(String name, Timestamp loginTime) {
        lastSaveTime.put(name, loginTime);
    }
}