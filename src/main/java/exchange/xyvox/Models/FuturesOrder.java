package exchange.xyvox.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "futures_order")
public class FuturesOrder extends Order{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "side", nullable = false)
    private String side;

    @Column(name = "leverage", nullable = false)
    private Integer leverage;

    @Column(name = "stopLoss", nullable = true)
    private Double stopLoss;

    @Column(name = "takeProfit", nullable = true)
    private Double takeProfit;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "transaction_id", nullable = true)
    private Transaction transaction;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @OneToOne(mappedBy = "futuresOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private FuturesPosition futuresPosition;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public Integer getLeverage() {
        return leverage;
    }

    public void setLeverage(Integer leverage) {
        this.leverage = leverage;
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



    public FuturesPosition getFuturesPosition() {
        return futuresPosition;
    }

    public void setFuturesPosition(FuturesPosition futuresPosition) {
        this.futuresPosition = futuresPosition;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
