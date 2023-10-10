package npc.bikathi.springstkpush.entity;

import jakarta.persistence.*;
import lombok.*;
import npc.bikathi.springstkpush.state.PaymentStatus;

import java.util.Date;

@Entity
@Table(name = "transaction_record")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "transaction_amount")
    private Long transactionAmount;

    @Column(name = "mobile_number")
    private Long mobileNumber;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_transaction")
    private Date dateOfTransaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;
}
