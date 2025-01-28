package botBank.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;


@Getter
@Setter
@ToString
@Entity
@Table(name = "transaction_detail")
public class TransactionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_card_number", nullable = true, length = 20)
    private String senderCardNumber;

    @OneToOne(mappedBy = "transactionDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User user;

    @Column(name = "sender_cvv", nullable = true, length = 4)
    private String senderCvv;

    @Column(name = "sender_exp_date", nullable = true, length = 10)
    private String senderExpDate;

    @Column(name = "recipient_card_number", nullable = true, length = 20)
    private String recipientCardNumber;

    @Column(name = "amount", nullable = true, precision = 15, scale = 2)
    private BigDecimal amount;


    public TransactionDetail() {}

    public TransactionDetail(String senderCardNumber, String senderCvv, String senderExpDate, String recipientCardNumber, BigDecimal amount) {
        this.senderCardNumber = senderCardNumber;
        this.senderCvv = senderCvv;
        this.senderExpDate = senderExpDate;
        this.recipientCardNumber = recipientCardNumber;
        this.amount = amount;
    }


}
