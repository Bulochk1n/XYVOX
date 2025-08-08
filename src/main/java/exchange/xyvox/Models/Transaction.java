package exchange.xyvox.Models;

import exchange.xyvox.Models.Enums.TransactionStatusEnum;
import exchange.xyvox.Models.Enums.TransactionTypeEnum;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "Transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private TransactionTypeEnum type;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "transaction_value", nullable = false)
    private Double transactionValue;

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMP")
    private Timestamp timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TransactionStatusEnum status;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @OneToOne(mappedBy = "transaction", fetch = FetchType.LAZY)
    private SpotOrder spotOrder;

    @OneToOne(mappedBy = "transaction", fetch = FetchType.LAZY)
    private FuturesOrder futuresOrder;


    @OneToOne(mappedBy = "transaction", fetch = FetchType.LAZY)
    private FuturesPosition futuresPosition;



    public Transaction() {}

    public Transaction(TransactionTypeEnum type, Double amount,
                       Double transactionValue, Timestamp timestamp,
                       TransactionStatusEnum status, Coin coin, Wallet wallet) {
        this.type = type;
        this.amount = amount;
        this.transactionValue = transactionValue;
        this.timestamp = timestamp;
        this.status = status;
        this.coin = coin;
        this.wallet = wallet;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TransactionTypeEnum getType() {
        return type;
    }

    public void setType(TransactionTypeEnum type) {
        this.type = type;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getTransactionValue() {
        return transactionValue;
    }

    public void setTransactionValue(Double value) {
        this.transactionValue = value;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionStatusEnum getStatus() {
        return status;
    }

    public void setStatus(TransactionStatusEnum status) {
        this.status = status;
    }

    public Coin getCoin() {
        return coin;
    }

    public void setCoin(Coin coin) {
        this.coin = coin;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public SpotOrder getSpotOrder() {
        return spotOrder;
    }

    public void setSpotOrder(SpotOrder spotOrder) {
        this.spotOrder = spotOrder;
    }

    public FuturesPosition getFuturesPosition() {
        return futuresPosition;
    }

    public void setFuturesPosition(FuturesPosition futuresPosition) {
        this.futuresPosition = futuresPosition;
    }

    public FuturesOrder getFuturesOrder() {
        return futuresOrder;
    }

    public void setFuturesOrder(FuturesOrder futuresOrder) {
        this.futuresOrder = futuresOrder;
    }
}
