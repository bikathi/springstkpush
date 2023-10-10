package npc.bikathi.springstkpush.controller;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import npc.bikathi.springstkpush.exception.PaymentRequestException;
import npc.bikathi.springstkpush.payload.request.InitiatePaymentRequest;
import npc.bikathi.springstkpush.payload.request.InitiateSTKPushReqBody;
import npc.bikathi.springstkpush.payload.response.CallbackResBody;
import npc.bikathi.springstkpush.payload.response.EndpointResponse;
import npc.bikathi.springstkpush.payload.response.InitiateSTKPushResBody;
import npc.bikathi.springstkpush.state.PaymentStatus;
import npc.bikathi.springstkpush.util.StkPushUtils;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
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

    @Value("${mpesa.callback.urlbase}")
    private String CALLBACK_URLBASE;

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
                    .CallBackURL(String.format("%s/api/v1/payment/callback", CALLBACK_URLBASE))
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

            // execute the request for initiating STK push request
            try (Response response = httpClient.newCall(stkPushRequest).execute()) {
                if (response.body() != null) {
                    InitiateSTKPushResBody responseObj = gson.fromJson(response.body().charStream(), InitiateSTKPushResBody.class);
                    if(!Objects.equals(responseObj.getResponseCode(), "0")) { // Object.equals() is null-safe
                        log.error("STK Push request failed to go through...");
                    }

                    log.info("Response body object: {}", responseObj);
                    // manually check for the payment status after manually waiting for 5 seconds
                    Thread.sleep(5000);

                    // check at max 7 times for the payment status with pauses of 5 seconds in between each check
                    verLoop:
                    for(int i = 0; i < 7; i++) {
                        PaymentStatus paymentStatus = stkPushUtils.verifyPaymentStatus(responseObj.getCheckoutRequestID(), b64PasswordString, timestamp, accessToken);
                        switch (paymentStatus) {
                            case STATUS_ACCEPTED -> {
                                log.info("Client has successfully paid...");
                                break verLoop;
                            }
                            case STATUS_CANCELLED -> {
                                log.info("Client has cancelled the transaction...");
                                return ResponseEntity.ok().body(new EndpointResponse(HttpStatus.OK.value(), new Date(), "Payment Cancelled By User", null));
                            }
                            case STATUS_PENDING -> {
                                log.info("Transaction is still processing...");
                                Thread.sleep(5000);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error occurred in the request: {}", e.getMessage());
                throw new PaymentRequestException("Encountered an Error. Please Try Again Later");
            }
        } catch(IOException ex) {
            log.error("IOException occurred: {}", ex.getMessage());
            throw new PaymentRequestException("Encountered an Error. Please Try Again Later");
        }

        // return response to the user
        return ResponseEntity.ok().body(new EndpointResponse(HttpStatus.OK.value(), new Date(), "Successful Payment", null));
    }
    @RequestMapping(value = "/callback")
    public void initiateSTKPushCallback(@org.springframework.web.bind.annotation.RequestBody CallbackResBody callbackResBody) {
        log.info("Callback response body: {}", callbackResBody.toString());
        if(callbackResBody.getBody().getStkCallback().getResultCode() != 0) {
            log.info("According to the callback, the payment did not go through...");
        }
    }
}
