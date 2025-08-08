package exchange.xyvox.Models;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Coin")
public class Coin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Symbol", nullable = false, unique = true)
    private String symbol;

    @Column(name = "Name", nullable = false, unique = true)
    private String name;

    @Column(name = "Network", nullable = false)
    private String network;

    @Column(name = "Decimals", nullable = false)
    private Integer decimals ;

    @Column(name = "Price", nullable = false)
    private Double price;

    @Column(name = "LastUpdatedAt", nullable = false)
    private Timestamp lastUpdatedAt;

    @OneToMany(mappedBy = "coin", fetch = FetchType.LAZY)
    private Set<WalletCoin> walletCoins = new HashSet<WalletCoin>();

    @OneToMany(
            mappedBy = "coin",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Transaction> transactions = new HashSet<>();

    @OneToMany(
            mappedBy = "coin",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<SpotOrder> spotOrders = new HashSet<>();

    @OneToMany(
            mappedBy = "coin",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<FuturesOrder> futuresOrders = new HashSet<>();


    public Set<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Date getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Timestamp lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public Set<WalletCoin> getWalletCoins() {
        return walletCoins;
    }

    public void setWalletCoins(Set<WalletCoin> walletCoins) {
        this.walletCoins = walletCoins;
    }
}
