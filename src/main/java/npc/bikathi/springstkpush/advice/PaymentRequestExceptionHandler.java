package npc.bikathi.springstkpush.advice;

import npc.bikathi.springstkpush.exception.PaymentRequestException;
import npc.bikathi.springstkpush.payload.response.EndpointResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

    @RestControllerAdvice
    public class PaymentRequestExceptionHandler {
        @ExceptionHandler(PaymentRequestException.class)
        @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
        public EndpointResponse paymentRequestException(PaymentRequestException ex, WebRequest request) {
                return new EndpointResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), new Date(), ex.getMessage(), request.getDescription(false));
        }
}
