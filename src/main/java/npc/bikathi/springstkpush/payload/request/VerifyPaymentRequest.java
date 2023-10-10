package npc.bikathi.springstkpush.payload.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class VerifyPaymentRequest {
    private String BusinessShortCode;
    private String Password;
    private String Timestamp;
    private String CheckoutRequestID;
}
