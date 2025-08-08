package exchange.xyvox.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "wallet_coin",
        uniqueConstraints = @UniqueConstraint(columnNames = {"wallet_id", "coin_id"}))
public class WalletCoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;

    @Column(name = "amount", nullable = false)
    private Double amount = 0.0;

    @Column(name = "coinvalue", nullable = false)
    private Double coinvalue = 0.0;

    public WalletCoin() { }

    public WalletCoin(Wallet wallet, Coin coin, Double amount) {
        this.wallet = wallet;
        this.coin = coin;
        this.amount = amount;
        this.coinvalue = amount*coin.getPrice();
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Wallet getWallet() {
        return wallet;
    }
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Coin getCoin() {
        return coin;
    }
    public void setCoin(Coin coin) {
        this.coin = coin;
    }

    public Double getAmount() {
        return amount;
    }
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getCoinvalue() {
        return coinvalue;
    }

    public void setCoinvalue(Double coinvalue) {
        this.coinvalue = coinvalue;
    }
}
