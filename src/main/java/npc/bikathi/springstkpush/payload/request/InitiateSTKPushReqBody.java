package npc.bikathi.springstkpush.payload.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class InitiateSTKPushReqBody {
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
