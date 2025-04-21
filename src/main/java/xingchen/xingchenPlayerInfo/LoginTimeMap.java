package xingchen.xingchenPlayerInfo;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginTimeMap {
    public static final Map<String, Timestamp> loginTimes = new ConcurrentHashMap<>();

    public static Timestamp getLoginTime(String name) {
        return loginTimes.get(name);
    }

    public static void removeLoginTime(String name) {
        loginTimes.remove(name);
    }

    public static void setLoginTime(String name, Timestamp loginTime) {
        loginTimes.put(name, loginTime);
    }
}