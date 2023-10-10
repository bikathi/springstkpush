package npc.bikathi.springstkpush.exception;

public class PaymentRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PaymentRequestException(String message) {
        super(message);
    }
}
