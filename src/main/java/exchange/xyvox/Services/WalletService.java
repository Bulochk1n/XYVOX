package exchange.xyvox.Services;

import exchange.xyvox.Models.*;
import exchange.xyvox.Models.Enums.TransactionStatusEnum;

import exchange.xyvox.Repositories.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    private final AppUserService appUserService;
    private final CoinService coinService;
    private final TransactionService transactionService;

    public WalletService(WalletRepository walletRepository,
                         AppUserService appUserService,
                         CoinService coinService, TransactionService transactionService) {
        this.walletRepository = walletRepository;
        this.appUserService = appUserService;
        this.coinService = coinService;
        this.transactionService = transactionService;
    }

    public void updateWalletCoinValues(Wallet wallet) {
        Set<WalletCoin> walletCoins = wallet.getWalletCoins();
        for (WalletCoin walletCoin : walletCoins) {
            BigDecimal amount = BigDecimal.valueOf(walletCoin.getAmount());
            BigDecimal price = BigDecimal.valueOf(walletCoin.getCoin().getPrice());
            BigDecimal value = amount.multiply(price).setScale(walletCoin.getCoin().getDecimals(), RoundingMode.HALF_UP);
            walletCoin.setCoinvalue(value.doubleValue());
        }
        walletRepository.save(wallet);
    }

    public Wallet getWalletByNetworkAndAddress(String network, String address) {
        return walletRepository.findByNetworkAndAddress(network, address).orElse(null);
    }

    public String getAddress(Integer walletId, String network) {
        Wallet wallet = walletRepository.findWalletById(walletId).orElse(null);
        if (wallet == null) {
            return null;
        }
        Map<String, String> addresses = wallet.getAddresses();
        if (!addresses.containsKey(network)) {
            String address = generateAddress();
            addresses.put(network, address);
            wallet.setAddresses(addresses);
            walletRepository.save(wallet);
        }
        return addresses.get(network);

    }

    public String generateAddress() {

        String HEX_CHARS = "0123456789abcdef";
        int ADDRESS_LENGTH = 40;
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder("0x");
        for (int i = 0; i < ADDRESS_LENGTH; i++) {
            int index = random.nextInt(HEX_CHARS.length());
            sb.append(HEX_CHARS.charAt(index));
        }
        return sb.toString();
    }

    public void updateCoinAmount(Integer walletId, Coin coin, Double amount,
                                 String side, Integer transactionId) {

        Wallet wallet = walletRepository.findWalletById(walletId).orElse(null);
        if (wallet == null) {
            return;
        }
        Set<WalletCoin> walletCoins = wallet.getWalletCoins();

        if (side.equals("buy")) {
            for (WalletCoin wc : walletCoins) {
                if (wc.getCoin().equals(coin)) {
                    wc.setAmount(wc.getAmount() + amount);
                    wallet.setWalletCoins(walletCoins);
                    walletRepository.save(wallet);
                    if (transactionId != null) {
                        transactionService.updateTransactionStatusById(transactionId,
                                TransactionStatusEnum.COMPLETED);
                    }
                    return;
                }
            }
            addCoin(walletId, coin, amount);
            if (transactionId != null) {
                transactionService.updateTransactionStatusById(transactionId,
                        TransactionStatusEnum.COMPLETED);
            }

            return;
        } else if (side.equals("sell")) {
            for (WalletCoin wc : walletCoins) {
                if (wc.getCoin().equals(coin)) {
                    wc.setAmount(wc.getAmount() - amount);
                    if (wc.getAmount() <= 0) {
                        removeCoin(walletId, coin);
                    }
                    wallet.setWalletCoins(walletCoins);
                    walletRepository.save(wallet);
                    if (transactionId != null) {
                        transactionService.updateTransactionStatusById(transactionId,
                                TransactionStatusEnum.COMPLETED);
                    }

                    return;
                }
            }
        }
        if (transactionId != null) {
            transactionService.updateTransactionStatusById(transactionId, TransactionStatusEnum.FAILED);
        }

    }

    public void addCoin(Integer walletId, Coin coin, Double amount) {
        Wallet wallet = walletRepository.findWalletById(walletId).orElse(null);
        if (wallet == null) {
            return;
        }
        Set<WalletCoin> walletCoins = wallet.getWalletCoins();
        walletCoins.add(new WalletCoin(wallet, coin, amount));
        walletRepository.save(wallet);
    }

    public void removeCoin(Integer walletId, Coin coin) {
        Wallet wallet = walletRepository.findWalletById(walletId).orElse(null);
        if (wallet == null) {
            return;
        }
        Set<WalletCoin> walletCoins = wallet.getWalletCoins();
        walletCoins.remove(coin);
        walletRepository.save(wallet);
    }

    public void sendCoins(Coin coin, Double amount, String recipientAddress,
                          String network, Integer transactionId) {

        Optional<Wallet> maybeWallet = walletRepository.findByNetworkAndAddress(network, recipientAddress);
        if (maybeWallet.isEmpty()) {
            return;
        }

        Wallet targetWallet = maybeWallet.get();
        updateCoinAmount(targetWallet.getId(), coin, amount, "buy", transactionId);
    }

    public void createWallet(Wallet wallet) {
        walletRepository.save(wallet);
    }

    public Double getWalletCoinAmount(Integer walletId, String symbol) {
        Wallet wallet = walletRepository.findWalletById(walletId).orElse(null);
        if (wallet == null) {
            return 0.0;
        }
        Set<WalletCoin> walletCoins = wallet.getWalletCoins();
        for (WalletCoin wc : walletCoins) {
            if (wc.getCoin().getSymbol().equals(symbol)) {
                return wc.getAmount();
            }
        }
        return null;
    }

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public Wallet getWalletById(Integer walletId) {
        return walletRepository.findWalletById(walletId).orElse(null);
    }

    public void addSpotOrder(Integer walletId, SpotOrder spotOrder) {
        Wallet wallet = walletRepository.findWalletById(walletId).orElse(null);
        if (wallet == null) {
            return;
        }
        Set<SpotOrder> spotOrders = wallet.getSpotOrders();
        spotOrders.add(spotOrder);
        wallet.setSpotOrders(spotOrders);
        walletRepository.save(wallet);
    }

    public void updateSpotOrder(Integer walletId, SpotOrder spotOrder) {
        Wallet wallet = walletRepository.findWalletById(walletId).orElse(null);
        if (wallet == null) {
            return;
        }
        Set<SpotOrder> spotOrders = wallet.getSpotOrders();
        for (SpotOrder s : spotOrders) {
            if (s.getId().equals(spotOrder.getId())) {
                s = spotOrder;
                wallet.setSpotOrders(spotOrders);
                walletRepository.save(wallet);
                return;
            }
        }
    }

    public void addFuturesOrder(Integer walletId, FuturesOrder futuresOrder) {
        Wallet wallet = walletRepository.findWalletById(walletId).orElse(null);
        if (wallet == null) {
            return;
        }
        Set<FuturesOrder> futuresOrders = wallet.getFuturesOrders();
        futuresOrders.add(futuresOrder);
        wallet.setFuturesOrders(futuresOrders);
        walletRepository.save(wallet);
    }

    public void updateFuturesOrder(Integer walletId, FuturesOrder futuresOrder) {
        Wallet wallet = walletRepository.findWalletById(walletId).orElse(null);
        if (wallet == null) {
            return;
        }
        Set<FuturesOrder> futuresOrders = wallet.getFuturesOrders();
        for (FuturesOrder s : futuresOrders) {
            if (s.getId().equals(futuresOrder.getId())) {
                s = futuresOrder;
                wallet.setFuturesOrders(futuresOrders);
                walletRepository.save(wallet);
                return;
            }
        }

    }

    public void addFuturesPosition(Integer walletId, FuturesPosition futuresPosition) {
        Wallet wallet = walletRepository.findWalletById(walletId).orElse(null);
        if (wallet == null) {
            return;
        }
        Set<FuturesPosition> futuresPositions = wallet.getFuturesPositions();
        futuresPositions.add(futuresPosition);
        wallet.setFuturesPositions(futuresPositions);
        walletRepository.save(wallet);
    }

    public void updateFuturesPosition(Integer walletId, FuturesPosition futuresPosition) {
        Wallet wallet = walletRepository.findWalletById(walletId).orElse(null);
        if (wallet == null) {
            return;
        }
        Set<FuturesPosition> futuresPositions = wallet.getFuturesPositions();
        for (FuturesPosition s : futuresPositions) {
            if (s.getId().equals(futuresPosition.getId())) {
                s = futuresPosition;
                wallet.setFuturesPositions(futuresPositions);
                walletRepository.save(wallet);
                return;
            }
        }
    }

}

