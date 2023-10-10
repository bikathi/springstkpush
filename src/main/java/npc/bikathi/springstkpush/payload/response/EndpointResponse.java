package npc.bikathi.springstkpush.payload.response;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EndpointResponse {
    private Integer statusCode;
    private Date timeStamp;
    private String message;
    private String description;
}
