package npc.bikathi.springstkpush.payload.response;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class CallbackResBody {
    private Body Body;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    @ToString
    public static class Body {
        private StkCallback stkCallback;

        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Getter
        @Setter
        @ToString
        public static class StkCallback {
            private String MerchantRequestID;
            private String CheckoutRequestID;
            private Integer ResultCode;
            private String ResultDesc;
            private CallbackMetadata CallbackMetadata;

            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @Getter
            @Setter
            @ToString
            static class CallbackMetadata {
                private Item Item;

                @NoArgsConstructor
                @AllArgsConstructor
                @Builder
                @Getter
                @Setter
                @ToString
                public static class Item {
                    private List<Items> itemsList;

                    @NoArgsConstructor
                    @AllArgsConstructor
                    @Builder
                    @Getter
                    @Setter
                    @ToString
                    public static class Items {
                        private String Name;
                        private Object Value;
                    }
                }
            }
        }
    }
}
