package exchange.xyvox.Services;


import exchange.xyvox.Models.*;
import exchange.xyvox.Models.Enums.FuturesPositionStatusEnum;
import exchange.xyvox.Models.Enums.OrderStatusEnum;
import exchange.xyvox.Models.Enums.TransactionStatusEnum;
import exchange.xyvox.Models.Enums.TransactionTypeEnum;
import exchange.xyvox.Repositories.CoinRepository;
import exchange.xyvox.Repositories.FuturesOrderRepository;
import exchange.xyvox.Repositories.FuturesPositionRepository;
import exchange.xyvox.Repositories.SpotOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.Future;

@Service
public class PriceUpdateService {

    private static final String BINANCE_URL = "https://api.binance.com/api/v3/ticker/price";

    private final CoinRepository coinRepository;
    private final RestTemplate restTemplate;
    private final WalletService walletService;
    private final SpotOrderRepository spotOrderRepository;
    private final CoinService coinService;
    private final FuturesOrderRepository futuresOrderRepository;
    private final TransactionService transactionService;
    private final FuturesPositionRepository futuresPositionRepository;


    public PriceUpdateService(CoinRepository coinRepository, WalletService walletService, SpotOrderRepository spotOrderRepository, CoinService coinService, FuturesOrderRepository futuresOrderRepository, TransactionService transactionService, FuturesPositionRepository futuresPositionRepository) {
        this.coinRepository = coinRepository;
        this.walletService = walletService;
        this.restTemplate = new RestTemplate();
        this.spotOrderRepository = spotOrderRepository;
        this.coinService = coinService;
        this.futuresOrderRepository = futuresOrderRepository;
        this.transactionService = transactionService;
        this.futuresPositionRepository = futuresPositionRepository;
    }

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void fetchAndUpdatePrices() {
        List<Coin> allCoins = (List<Coin>) coinRepository.findAll();
        if (allCoins.isEmpty()) {
            return;
        }

        try {
            ParameterizedTypeReference<List<Map<String, String>>> typeRef =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<List<Map<String, String>>> resp = restTemplate.exchange(
                    BINANCE_URL,
                    HttpMethod.GET,
                    null,
                    typeRef
            );

            if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
                return;
            }

            List<Map<String, String>> allPrices = resp.getBody();

            Map<String, Double> priceMap = new HashMap<>();
            for (Map<String, String> obj : allPrices) {
                String sym = obj.get("symbol");
                String priceStr = obj.get("price");
                if (sym != null && priceStr != null) {
                    try {
                        Double p = Double.valueOf(priceStr);
                        priceMap.put(sym, p);
                    } catch (NumberFormatException ignore) {}
                }
            }

            for (Coin coin : allCoins) {
                String symbol = coin.getSymbol() + "USDT";
                if (priceMap.containsKey(symbol)) {
                    Double newPrice = priceMap.get(symbol);
                    if (!Objects.equals(newPrice, coin.getPrice())) {
                        coin.setPrice(newPrice);
                        System.out.print(coin.getSymbol() + " " + coin.getPrice() + " ");
                        coinRepository.save(coin);
                    }
                }
            }

            List<Wallet> wallets = walletService.getAllWallets();
            for (Wallet wallet : wallets) {
                walletService.updateWalletCoinValues(wallet);
            }

            checkAndExecuteOpenSpotOrders();
            checkAndExecuteOpenFuturesOrders();


            System.out.println();
        } catch (Exception ex) {
            System.err.println("Failed to update prices from Binance: " + ex.getMessage());
        }
    }

    @Transactional
    public void checkAndExecuteOpenSpotOrders() {

        List<SpotOrder> openSpotOrders = spotOrderRepository.findByStatus(OrderStatusEnum.OPEN);
        if (openSpotOrders.isEmpty()) {
            return;
        }

        List<Coin> coinsFromOpenOrders = openSpotOrders.stream().map(SpotOrder::getCoin).toList();

        Map<String, Double> currentPrices = coinService.fetchCurrentPricesForSymbols(
                coinsFromOpenOrders.stream()
                        .map(Coin::getSymbol)   // symbol: "ETH", "ADA"
                        .distinct()
                        .toList()
        );


        for (SpotOrder order : openSpotOrders) {
            String symbol = order.getCoin().getSymbol();
            Double marketPrice = currentPrices.get(symbol);
            if (marketPrice == null) {
                continue;
            }

            boolean shouldExecute = false;
            if (order.getSide().equalsIgnoreCase("buy")) {
                if (marketPrice <= order.getPrice()) {
                    shouldExecute = true;
                }
            } else if (order.getSide().equalsIgnoreCase("sell")) {
                if (marketPrice >= order.getPrice()) {
                    shouldExecute = true;
                }
            }

            if (shouldExecute) {
                executeSpotOrder(order, marketPrice);
            }
        }

    }

    private void executeSpotOrder(SpotOrder spotOrder, Double executionPrice) {

        spotOrder.setStatus(OrderStatusEnum.EXECUTED);
        spotOrder.setPrice(executionPrice);

        Double amount_rounded = BigDecimal.valueOf(spotOrder.getOrderValue() / executionPrice).setScale(spotOrder.getCoin().getDecimals(), RoundingMode.HALF_DOWN).doubleValue();


        spotOrder.setAmount(amount_rounded);
        spotOrderRepository.save(spotOrder);

        if (spotOrder.getSide().equals("buy")) {
            walletService.updateCoinAmount(spotOrder.getWallet().getId(), coinService.getCoinBySymbol(spotOrder.getCoin().getSymbol()), spotOrder.getAmount(), "buy", spotOrder.getTransaction().getId());
        } else if (spotOrder.getSide().equals("sell")) {
            walletService.updateCoinAmount(spotOrder.getWallet().getId(), coinService.getCoinBySymbol("USDT"), spotOrder.getOrderValue(), "buy", spotOrder.getTransaction().getId());
        }

        System.out.println("order executed successfully " + spotOrder.getSide() + " " + spotOrder.getAmount() + " " + spotOrder.getTransaction().getId() );
    }

    @Transactional
    public void checkAndExecuteOpenFuturesOrders() {

        List<FuturesOrder> openFuturesOrders = futuresOrderRepository.findByStatus(OrderStatusEnum.OPEN);
        if (openFuturesOrders.isEmpty()) {
            return;
        }

        List<Coin> coinsFromOpenOrders = openFuturesOrders.stream().map(FuturesOrder::getCoin).toList();

        Map<String, Double> currentPrices = coinService.fetchCurrentPricesForSymbols(
                coinsFromOpenOrders.stream()
                        .map(Coin::getSymbol)   // symbol: "ETH", "ADA"
                        .distinct()
                        .toList()
        );


        for (FuturesOrder order : openFuturesOrders) {
            String symbol = order.getCoin().getSymbol();
            Double marketPrice = currentPrices.get(symbol);
            if (marketPrice == null) {
                continue;
            }

            if (marketPrice <= order.getPrice()*(1.0001) && marketPrice >= order.getPrice()*(0.9999)) {
                executeFuturesOrder(order, marketPrice);
            }
        }

    }

    @Transactional
    public void executeFuturesOrder(FuturesOrder futuresOrder, Double executionPrice) {


        Double amount_rounded = BigDecimal.valueOf(futuresOrder.getOrderValue() / executionPrice).setScale(futuresOrder.getCoin().getDecimals(), RoundingMode.HALF_DOWN).doubleValue();

        // 1) Обновляем существующий ордер:
        futuresOrder.setStatus(OrderStatusEnum.EXECUTED);
        futuresOrder.setPrice(executionPrice);
        futuresOrder.setAmount(amount_rounded);
        transactionService.updateTransactionStatusById(futuresOrder.getTransaction().getId(), TransactionStatusEnum.COMPLETED);

// 2) Создаём новую позицию и связываем её с ордером:
        FuturesPosition futuresPosition = new FuturesPosition();
        futuresPosition.setEntryPrice(executionPrice);
        futuresPosition.setAmount(amount_rounded);
        futuresPosition.setProvidedValue(futuresOrder.getOrderValue());
        futuresPosition.setLeverage(futuresOrder.getLeverage());
        futuresPosition.setSide(futuresOrder.getSide());
        futuresPosition.setStatus(FuturesPositionStatusEnum.OPEN);
        futuresPosition.setStopLoss(futuresOrder.getStopLoss());
        futuresPosition.setTakeProfit(futuresOrder.getTakeProfit());
        futuresPosition.setWallet(futuresOrder.getWallet());

// Создаём транзакцию и привязываем её:
        TransactionTypeEnum type = futuresPosition.getSide().equals("long")
                ? TransactionTypeEnum.LONG
                : TransactionTypeEnum.SHORT;
        Transaction transaction = transactionService.createTransaction(
                futuresOrder.getWallet().getId(),
                futuresOrder.getCoin().getId(),
                type,
                amount_rounded,
                futuresPosition.getProvidedValue()
        );
        futuresPosition.setTransaction(transaction);

// Устанавливаем связь «два‑к‑двум»:
        futuresPosition.setFuturesOrder(futuresOrder);
        futuresOrder.setFuturesPosition(futuresPosition);

        System.out.println("Trying to save ");
// 3) Сохраняем только ордер — благодаря Cascade.ALL Hibernate сам вставит POSITION:
        FuturesPosition savedPos = futuresPositionRepository.save(futuresPosition);
        Integer generatedId = savedPos.getId();
        System.out.println("saved " + generatedId + " " + savedPos.getSide() + " " + savedPos.getAmount() + " " + savedPos.getFuturesOrder().getCoin().getSymbol());
    }

}