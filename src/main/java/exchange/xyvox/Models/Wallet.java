package exchange.xyvox.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "Wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(mappedBy = "wallet", fetch = FetchType.LAZY)
    private AppUser owner;

    @ElementCollection
    @CollectionTable(name = "wallet_address", joinColumns = @JoinColumn(name = "wallet_id"))
    @MapKeyColumn(name = "network")
    @Column(name = "address")
    private Map<String, String> addresses = new HashMap<>();

    @OneToMany(
            mappedBy = "wallet", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY
    )
    private Set<WalletCoin> walletCoins = new HashSet<>();

    @OneToMany(
            mappedBy = "wallet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Transaction> transactions = new HashSet<>();

    @OneToMany(
            mappedBy = "wallet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<SpotOrder> spotOrders = new HashSet<>();

    @OneToMany(
            mappedBy = "wallet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<FuturesOrder> futuresOrders = new HashSet<>();

    @OneToMany(
            mappedBy = "wallet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<FuturesPosition> futuresPositions = new HashSet<>();



    public Set<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Map<String, String> getAddresses() {
        return addresses;
    }

    public void setAddresses(Map<String, String> addresses) {
        this.addresses = addresses;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    public Set<WalletCoin> getWalletCoins() {
        return walletCoins;
    }

    public void setWalletCoins(Set<WalletCoin> walletCoins) {
        this.walletCoins = walletCoins;
    }

    public Set<SpotOrder> getSpotOrders() {
        return spotOrders;
    }

    public void setSpotOrders(Set<SpotOrder> spotOrders) {
        this.spotOrders = spotOrders;
    }

    public Set<FuturesOrder> getFuturesOrders() {
        return futuresOrders;
    }

    public void setFuturesOrders(Set<FuturesOrder> futuresOrders) {
        this.futuresOrders = futuresOrders;
    }

    public Set<FuturesPosition> getFuturesPositions() {
        return futuresPositions;
    }

    public void setFuturesPositions(Set<FuturesPosition> futuresPositions) {
        this.futuresPositions = futuresPositions;
    }
}
