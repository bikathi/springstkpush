package npc.bikathi.springstkpush.controller;

import com.google.gson.Gson;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import npc.bikathi.springstkpush.payload.request.InitiatePaymentRequest;
import npc.bikathi.springstkpush.util.StkPushUtils;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/payment")
public class PaymentController {
    @Autowired
    private StkPushUtils stkPushUtils;

    @Value("${mpesa.business.shortcode}")
    private String SHORT_CODE;

    @Value("${mpesa.public.passkey}")
    private String PASSWORD_KEY;

    @Value("${mpesa.callback.url}")
    private String CALLBACK_URL;

    private final Gson gson = new Gson();

    @PostMapping(value = "/initiate")
    public ResponseEntity<?> initiateSTKPushRequest(@org.springframework.web.bind.annotation.RequestBody InitiatePaymentRequest paymentRequest) {
        // extract mobile and payment details from request body
        try {
            // wait for the access token to be generated
            String accessToken = stkPushUtils.requestAuthToken();

            // wait for a timestamp to be generated
            String timestamp = stkPushUtils.generateTimestamp();

            // wait for the base64 password String
            String b64PasswordString = stkPushUtils.generateB64PassString(SHORT_CODE, PASSWORD_KEY, timestamp);
            log.info("Timestamp generated: {}", timestamp);
            log.info("Access token successfully gotten: {}", accessToken);
            log.info("Base64 Password String: {}", b64PasswordString);

            // get a new OKHTTP client
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            // the request body
            InitiateSTKPushReqBody stkPushReqBody = InitiateSTKPushReqBody.builder()
                    .BusinessShortCode(SHORT_CODE)
                    .Password(b64PasswordString)
                    .Timestamp(timestamp)
                    .TransactionType("CustomerPayBillOnline")
                    .Amount(paymentRequest.getAmount())
                    .PartyA("254113883976")
                    .PartyB(SHORT_CODE)
                    .PhoneNumber(paymentRequest.getNumber())
                    .CallBackURL(CALLBACK_URL)
                    .AccountReference("SpringAppLTD")
                    .TransactionDesc("STK Test")
                    .build();
            String stkPushReqBodyJson = gson.toJson(stkPushReqBody);
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"), stkPushReqBodyJson);

            // build the request
            Request stkPushRequest = new Request.Builder()
                    .url("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest")
                    .method("POST", requestBody)
                    .header("Authorization", String.format("Bearer %s", accessToken))
                    .build();

            // execute the request
            try (Response response = httpClient.newCall(stkPushRequest).execute()) {
                if (response.body() != null) {
                    InitiateSTKPushResBody responseObj = gson.fromJson(response.body().charStream(), InitiateSTKPushResBody.class);
                    log.info("Response body object: {}", responseObj.toString());
                }
            }
        } catch(IOException ex) {
            log.error("IOException occurred: {}", ex.getMessage());
        }


        // initiate STK push request

        // wait for out callback to run at max 7 times with intervals of 5 seconds between

        // if the callback fails, manually check for the payment status

        // return response to the user
        return null;
    }
}

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
class InitiateSTKPushReqBody {
    private String BusinessShortCode;
    private String Password;
    private String Timestamp;
    private String TransactionType;
    private String Amount;
    private String PartyA;
    private String PartyB;
    private String PhoneNumber;
    private String CallBackURL;
    private String AccountReference;
    private String TransactionDesc;
}

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
class InitiateSTKPushResBody {
    private String MerchantRequestID;
    private String CheckoutRequestID;
    private String ResponseCode;
    private String ResponseDescription;
    private String CustomerMessage;
}
