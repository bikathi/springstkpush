package npc.bikathi.springstkpush.payload.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InitiatePaymentRequest {
    private String number;
    private String amount;
}
