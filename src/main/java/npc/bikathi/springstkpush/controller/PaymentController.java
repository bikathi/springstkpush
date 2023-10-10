package npc.bikathi.springstkpush.controller;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import npc.bikathi.springstkpush.entity.TransactionRecord;
import npc.bikathi.springstkpush.exception.PaymentRequestException;
import npc.bikathi.springstkpush.payload.request.InitiatePaymentRequest;
import npc.bikathi.springstkpush.payload.request.InitiateSTKPushReqBody;
import npc.bikathi.springstkpush.payload.response.CallbackResBody;
import npc.bikathi.springstkpush.payload.response.EndpointResponse;
import npc.bikathi.springstkpush.payload.response.InitiateSTKPushResBody;
import npc.bikathi.springstkpush.service.TransactionRecordService;
import npc.bikathi.springstkpush.state.PaymentStatus;
import npc.bikathi.springstkpush.util.StkPushUtils;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @Autowired
    private TransactionRecordService transactionRecordService;

    @Value("${mpesa.business.shortcode}")
    private String SHORT_CODE;

    @Value("${mpesa.public.passkey}")
    private String PASSWORD_KEY;

    @Value("${mpesa.callback.urlbase}")
    private String CALLBACK_URLBASE;

    private final Gson gson = new Gson();

    @PostMapping(value = "/initiate")
    public ResponseEntity<?> initiateSTKPushRequest(@org.springframework.web.bind.annotation.RequestBody InitiatePaymentRequest paymentRequest) {
        TransactionRecord newTransactionRecord = TransactionRecord.builder()
                .transactionAmount(Long.valueOf(paymentRequest.getAmount()))
                .mobileNumber(Long.valueOf(paymentRequest.getNumber()))
                .dateOfTransaction(new Date())
                .paymentStatus(PaymentStatus.STATUS_PENDING)
                .build();

        try {
            // save transaction record in DB
            TransactionRecord transactionRecord = transactionRecordService.insertTransaction(newTransactionRecord);

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
                    .CallBackURL(String.format("%s/api/v1/payment/callback?transactionId=%s", CALLBACK_URLBASE, transactionRecord.getTransactionId()))
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
                        return ResponseEntity.internalServerError().body(new EndpointResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), new Date(), "STK Push Request Denied", null));
                    }

                    log.info("STK Push request went through...");
                    // manually check for the payment status after waiting for 5 seconds
                    Thread.sleep(5000);

                    // check at max 7 times for the payment status with pauses of 5 seconds in between each check
                    verLoop:
                    for(int i = 0; i < 7; i++) {
                        PaymentStatus paymentStatus = stkPushUtils.verifyPaymentStatus(responseObj.getCheckoutRequestID(), b64PasswordString, timestamp, accessToken);
                        switch (paymentStatus) {
                            case STATUS_ACCEPTED -> {
                                log.info("Client has successfully paid...");
                                // update the transaction status
                                transactionRecord.setPaymentStatus(PaymentStatus.STATUS_ACCEPTED);
                                transactionRecordService.insertTransaction(transactionRecord);

                                break verLoop;
                            }
                            case STATUS_CANCELLED -> {
                                log.info("Client has cancelled the transaction...");
                                // update the transaction status
                                transactionRecord.setPaymentStatus(PaymentStatus.STATUS_CANCELLED);
                                transactionRecordService.insertTransaction(transactionRecord);

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
                return ResponseEntity.internalServerError().body(new EndpointResponse(HttpStatus.OK.value(), new Date(), "Encountered an Error. Please Try Again Later", null));
            }
        } catch(IOException ex) {
            log.error("IOException occurred: {}", ex.getMessage());
            return ResponseEntity.internalServerError().body(new EndpointResponse(HttpStatus.OK.value(), new Date(), "Encountered an Error. Please Try Again Later", null));
        }

        // return response to the user
        return ResponseEntity.ok().body(new EndpointResponse(HttpStatus.OK.value(), new Date(), "Successful Payment", null));
    }
    @RequestMapping(value = "/callback")
    public void initiateSTKPushCallback(
            @org.springframework.web.bind.annotation.RequestBody CallbackResBody callbackResBody, @RequestParam String transactionId
    ) {
        log.info("Callback response body: {}", callbackResBody.toString());
        Long id = Long.valueOf(transactionId);
        TransactionRecord existingRecord = transactionRecordService.retrieveExistingTransaction(id).orElseThrow();
        if(callbackResBody.getBody().getStkCallback().getResultCode() != 0) { // if it's not zero then the payment didn't go through, which we take as a cancel
            log.info("According to the callback, the payment did not go through...");
            existingRecord.setPaymentStatus(PaymentStatus.STATUS_CANCELLED);
            transactionRecordService.insertTransaction(existingRecord);
        } else {
            existingRecord.setPaymentStatus(PaymentStatus.STATUS_ACCEPTED);
            transactionRecordService.insertTransaction(existingRecord);
        }
    }
}
