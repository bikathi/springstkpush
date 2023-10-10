package npc.bikathi.springstkpush.controller;

import lombok.extern.slf4j.Slf4j;
import npc.bikathi.springstkpush.util.StkPushUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/payment")
public class PaymentController {
    @Autowired
    private StkPushUtils stkPushUtils;

    @PostMapping(value = "/initiate")
    public ResponseEntity<?> initiateSTKPushRequest() {
        // extract mobile and payment details from request body

        // wait for the auth token to be generated

        // initiate STK push request

        // wait for out callback to run at max 7 times with intervals of 5 seconds between

        // if the callback fails, manually check for the payment status

        // return response to the user

    }
}
