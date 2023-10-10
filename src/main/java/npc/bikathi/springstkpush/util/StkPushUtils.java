package npc.bikathi.springstkpush.util;

import com.google.gson.Gson;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import npc.bikathi.springstkpush.payload.request.VerifyPaymentRequest;
import npc.bikathi.springstkpush.payload.response.PaymentVerStatusResBody;
import npc.bikathi.springstkpush.state.PaymentStatus;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Base64;
import java.util.Objects;
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

    @Value("${mpesa.business.shortcode}")
    private String SHORT_CODE;

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

    public PaymentStatus verifyPaymentStatus
        (@NotNull String checkoutRequestId, @NotNull String password, @NotNull String timeStamp, @NotNull String accessToken) throws java.io.IOException {
        log.info("Attempting to check status of the payment...");

        // build the request body
        VerifyPaymentRequest verifyPaymentRequest = new VerifyPaymentRequest(SHORT_CODE, password, timeStamp, checkoutRequestId);
        String verifyPaymentRequestJson = gson.toJson(verifyPaymentRequest);
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), verifyPaymentRequestJson);

        // get a new OKHTTP client
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        // build the request
        Request paymentStatusRequest = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/mpesa/stkpushquery/v1/query")
                .method("POST", requestBody)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .build();

        // execute the request
        try (Response response = httpClient.newCall(paymentStatusRequest).execute()) {
            if (response.body() != null) {
                PaymentVerStatusResBody responseBody = gson.fromJson(response.body().charStream(), PaymentVerStatusResBody.class);
                log.info("Gotten response body: {}", responseBody.toString());
                if(Objects.equals(responseBody.getResponseCode(), "0")) { // the STK push popup was closed on the client's phone either by paying or by cancelling
                    if(!Objects.equals(responseBody.getResultCode(), "0")) { // if the result code is not 0 it means the client cancelled the transaction
                        log.info("client cancelled the transaction...");
                        return PaymentStatus.STATUS_CANCELLED;
                    }

                    log.info("Client paid the requested amount...");
                    return PaymentStatus.STATUS_ACCEPTED;
                }
            }
        }

        return PaymentStatus.STATUS_PENDING;
    }

    public String generateTimestamp() {
        LocalDateTime timeNow = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return timeNow.format(formatter);
    }

    public String generateB64PassString(@NotNull String shortCode, @NotNull String passwordKey, @NotNull String timeStamp) {
        return Base64.getEncoder().encodeToString(String.format("%s%s%s", shortCode, passwordKey, timeStamp).getBytes());
    }

    private static class AuthTokenResponse {
        String access_token;
        String expires_in;
    }
}
