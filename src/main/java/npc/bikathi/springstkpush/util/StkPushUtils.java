package npc.bikathi.springstkpush.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StkPushUtils {
    @Value("${mpesa.consumer.key}")
    private String CONSUMER_KEY;

    @Value("${mpesa.consumer.secret}")
    private String COMSUMER_SECRET;

    public static String requestAuthToken() {
        return null;
    }

    public static String generateTimestampString() {
        return null;
    }

    public static String generateB64PassString() {
        return null;
    }
}
