package eu.greev.twofa.utils;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public class HashingUtils {

    //TODO: Hash IPs before they get added to the Database
    public static String hashIp(String ip) {
        return Hashing.sha256().hashString(ip, StandardCharsets.UTF_8).toString();
    }

}
