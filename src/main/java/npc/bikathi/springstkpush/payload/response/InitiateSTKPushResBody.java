package npc.bikathi.springstkpush.payload.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class InitiateSTKPushResBody {
    private String MerchantRequestID;
    private String CheckoutRequestID;
    private String ResponseCode;
    private String ResponseDescription;
    private String CustomerMessage;
}
