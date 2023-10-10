package npc.bikathi.springstkpush.payload.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class PaymentVerStatusResBody {
    private String ResponseCode;
    private String ResponseDescription;
    private String MerchantRequestID;
    private String CheckoutRequestID;
    private String ResultCode;
    private String ResultDesc;
}
