package npc.bikathi.springstkpush.util;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class StkPushUtils {
    @Value("${mpesa.consumer.key}")
    private String CONSUMER_KEY;

    @Value("${mpesa.consumer.secret}")
    private String CONSUMER_SECRET;

    private final Gson gson = new Gson();
    private String accessToken;

    public String requestAuthToken() throws java.io.IOException {
        log.info("Attempting to generate access token...");
        // get a new OKHTTP client
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        // create a Base64 String of 'CONSUMER_KEY:CONSUMER_SECRET'
        final String encodedString = Base64.getEncoder().encodeToString(String.format("%s:%s", CONSUMER_KEY, CONSUMER_SECRET).getBytes());
        log.info("Encoded string: {}", encodedString);

        // build the request
        Request authTokenRequest = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                .method("GET", null)
                .header("Authorization", String.format("Basic %s", encodedString))
                .build();

        // execute the request
        try (Response response = httpClient.newCall(authTokenRequest).execute()) {
            if (response.body() != null) {
                AuthTokenResponse responseObj = gson.fromJson(response.body().charStream(), AuthTokenResponse.class);
                accessToken = responseObj.access_token;
                log.info("Gotten access token: {}", accessToken);
            }
        }
        return accessToken;
    }

    public String generateTimestamp() {
        LocalDateTime timeNow = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return timeNow.format(formatter);
    }

    public String generateB64PassString(@NotNull String shortCode, @NotNull String passwordkey, @NotNull String timeStamp) {
        String encodedString = Base64.getEncoder().encodeToString(String.format("%s%s%s", shortCode, passwordkey, timeStamp).getBytes());
        return encodedString;
    }

    private static class AuthTokenResponse {
        String access_token;
        String expires_in;
    }
}
