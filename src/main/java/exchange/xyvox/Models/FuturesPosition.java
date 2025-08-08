package exchange.xyvox.Models;

import exchange.xyvox.Models.Enums.FuturesPositionStatusEnum;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "futures_position")
public class FuturesPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "entryPrice", nullable = false)
    private Double entryPrice;

    // provided_value * leverage/ entryPrice
    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "provided_value", nullable = false)
    private Double providedValue;

    @Column(name = "leverage", nullable = false)
    private Integer leverage;

    @Column(name = "side", length = 20, nullable = false)
    private String side;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private FuturesPositionStatusEnum status;

    @Column(name = "stopLoss", nullable = true)
    private Double stopLoss;

    @Column(name = "takeProfit", nullable = true)
    private Double takeProfit;

    @Column(name = "exitPrice", nullable = true)
    private Double exitPrice;

    @Column(name = "pnl", nullable = true)
    private Double pnl;

    @Column(name = "openedAt", nullable = false)
    private Timestamp openedAt;

    @Column(name = "closedAt", nullable = true)
    private Timestamp closedAt;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "transaction_id", nullable = true)
    private Transaction transaction;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "futures_order_id", unique = true)
    private FuturesOrder futuresOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    public FuturesPosition() {
        this.openedAt = new Timestamp(System.currentTimeMillis());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(Double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getProvidedValue() {
        return providedValue;
    }

    public void setProvidedValue(Double provided_value) {
        this.providedValue = provided_value;
    }

    public Integer getLeverage() {
        return leverage;
    }

    public void setLeverage(Integer leverage) {
        this.leverage = leverage;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public FuturesPositionStatusEnum getStatus() {
        return status;
    }

    public void setStatus(FuturesPositionStatusEnum status) {
        this.status = status;
    }

    public Double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(Double stopLoss) {
        this.stopLoss = stopLoss;
    }

    public Double getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(Double takeProfit) {
        this.takeProfit = takeProfit;
    }

    public Double getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(Double exitPrice) {
        this.exitPrice = exitPrice;
    }

    public Double getPnl() {
        return pnl;
    }

    public void setPnl(Double pnl) {
        this.pnl = pnl;
    }

    public Timestamp getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(Timestamp openedAt) {
        this.openedAt = openedAt;
    }

    public Timestamp getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Timestamp closedAt) {
        this.closedAt = closedAt;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public FuturesOrder getFuturesOrder() {
        return futuresOrder;
    }

    public void setFuturesOrder(FuturesOrder futuresOrder) {
        this.futuresOrder = futuresOrder;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
}
