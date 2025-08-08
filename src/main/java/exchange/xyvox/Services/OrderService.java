package exchange.xyvox.Services;

import exchange.xyvox.Models.*;
import exchange.xyvox.Models.Enums.OrderStatusEnum;
import exchange.xyvox.Models.Enums.OrderTypeEnum;
import exchange.xyvox.Repositories.FuturesOrderRepository;
import exchange.xyvox.Repositories.SpotOrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

@Service
public class OrderService {

    private final SpotOrderRepository spotOrderRepository;
    private final FuturesOrderRepository futuresOrderRepository;
    private final CoinService coinService;
    private final WalletService walletService;
    private final TransactionService transactionService;
    private final AppUserService appUserService;


    public OrderService(SpotOrderRepository spotOrderRepository, FuturesOrderRepository futuresOrderRepository, CoinService coinService, WalletService walletService, TransactionService transactionService, AppUserService appUserService) {
        this.spotOrderRepository = spotOrderRepository;
        this.futuresOrderRepository = futuresOrderRepository;
        this.coinService = coinService;
        this.walletService = walletService;
        this.transactionService = transactionService;
        this.appUserService = appUserService;
    }

    public SpotOrder createSpotOrder(String symbol, String side, Double execPrice,
                                     Double amount, Double value,
                                     Integer walletId, Transaction transaction, String type) {
        SpotOrder spotOrder = new SpotOrder();

        if (type.equalsIgnoreCase("limit")) {
            spotOrder.setType(OrderTypeEnum.LIMIT);
            spotOrder.setStatus(OrderStatusEnum.OPEN);
        } else if (type.equalsIgnoreCase("market")) {
            spotOrder.setType(OrderTypeEnum.MARKET);
            spotOrder.setStatus(OrderStatusEnum.EXECUTED);
        }

        Coin coin = coinService.getCoinBySymbol(symbol);

        Double executionPrice_rounded = BigDecimal.valueOf(execPrice).setScale(coin.getDecimals(), RoundingMode.HALF_UP).doubleValue();
        Double amount_rounded = BigDecimal.valueOf(amount).setScale(coin.getDecimals(), RoundingMode.HALF_DOWN).doubleValue();
        Double value_rounded = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_DOWN).doubleValue();


        spotOrder.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        spotOrder.setPrice(executionPrice_rounded);
        spotOrder.setCoin(coin);
        spotOrder.setWallet(walletService.getWalletById(walletId));
        spotOrder.setSide(side);
        spotOrder.setAmount(amount_rounded);
        spotOrder.setOrderValue(value_rounded);
        spotOrder.setTransaction(transaction);

        //blocking money for transaction
        if(type.equalsIgnoreCase("limit")) {
            if (side.equals("buy")) {
                walletService.updateCoinAmount(walletId, coinService.getCoinBySymbol("USDT"), value_rounded, "sell", null);
            } else if (side.equals("sell")) {
                walletService.updateCoinAmount(walletId, coin, amount_rounded, "sell", null);
            }
        }

        System.out.println("spot order created " + spotOrder.getCoin().getSymbol() + " " + spotOrder.getAmount());
        return spotOrderRepository.save(spotOrder);
    }

    public SpotOrder findSpotOrderById(Integer orderId) {
        return spotOrderRepository.findById(orderId).orElse(null);
    }

    public void updateSpotOrderStatus(Integer orderId, OrderStatusEnum status) {
        SpotOrder spotOrder = findSpotOrderById(orderId);
        spotOrder.setStatus(status);
        spotOrderRepository.save(spotOrder);
        walletService.updateSpotOrder(spotOrder.getWallet().getId(), spotOrder);
    }

    public void closeSpotOrder(SpotOrder spotOrder) {
        spotOrder.setStatus(OrderStatusEnum.CANCELLED);
        spotOrderRepository.save(spotOrder);
        if (spotOrder.getSide().equals("buy")) {
            walletService.updateCoinAmount(spotOrder.getWallet().getId(), coinService.getCoinBySymbol("USDT"), spotOrder.getOrderValue(), "buy", null);
        } else if (spotOrder.getSide().equals("sell")) {
            walletService.updateCoinAmount(spotOrder.getWallet().getId(), coinService.getCoinBySymbol(spotOrder.getCoin().getSymbol()), spotOrder.getAmount(), "buy", null);
        }
        walletService.updateSpotOrder(spotOrder.getWallet().getId(), spotOrder);
    }

    public FuturesOrder createFuturesOrder(String symbol, String side, Double execPrice,
                                           Double amount, Double value, Integer leverage,
                                           Double stopLoss, Double takeProfit,
                                           Integer walletId, String type, Transaction transaction) {
        FuturesOrder futuresOrder = new FuturesOrder();

        if (type.equalsIgnoreCase("limit")) {
            futuresOrder.setType(OrderTypeEnum.LIMIT);
            futuresOrder.setStatus(OrderStatusEnum.OPEN);
        } else if (type.equalsIgnoreCase("market")) {
            futuresOrder.setType(OrderTypeEnum.MARKET);
            futuresOrder.setStatus(OrderStatusEnum.EXECUTED);
        }

        Coin coin = coinService.getCoinBySymbol(symbol);

        Double executionPrice_rounded = BigDecimal.valueOf(execPrice).setScale(coin.getDecimals(), RoundingMode.HALF_UP).doubleValue();
        Double amount_rounded = BigDecimal.valueOf(amount).setScale(coin.getDecimals(), RoundingMode.HALF_DOWN).doubleValue();
        Double value_rounded = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_DOWN).doubleValue();


        futuresOrder.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        futuresOrder.setPrice(executionPrice_rounded);
        futuresOrder.setCoin(coin);
        futuresOrder.setWallet(walletService.getWalletById(walletId));
        futuresOrder.setSide(side);
        futuresOrder.setLeverage(leverage);
        futuresOrder.setTransaction(transaction);

        futuresOrder.setStopLoss(stopLoss);
        futuresOrder.setTakeProfit(takeProfit);

        futuresOrder.setAmount(amount_rounded);
        futuresOrder.setOrderValue(value_rounded);


        futuresOrder.setFuturesPosition(null);


        walletService.updateCoinAmount(walletId, coinService.getCoinBySymbol("USDT"), value_rounded, "sell", null);


        System.out.println("futures order created " + futuresOrder.getCoin().getSymbol() + " " + futuresOrder.getAmount());
        return futuresOrderRepository.save(futuresOrder);
    }

    public FuturesOrder findFuturesOrderById(Integer orderId) {
        return futuresOrderRepository.findById(orderId).orElse(null);
    }

    public void updateFuturesOrderStatus(Integer orderId, OrderStatusEnum status) {
        FuturesOrder futuresOrder = findFuturesOrderById(orderId);
        futuresOrder.setStatus(status);
        futuresOrderRepository.save(futuresOrder);
        walletService.updateFuturesOrder(futuresOrder.getWallet().getId(), futuresOrder);
    }

    public void closeFuturesOrder(FuturesOrder futuresOrder) {
        futuresOrder.setStatus(OrderStatusEnum.CANCELLED);
        futuresOrderRepository.save(futuresOrder);
        walletService.updateCoinAmount(futuresOrder.getWallet().getId(), coinService.getCoinBySymbol("USDT"), futuresOrder.getOrderValue(), "buy", null);
        walletService.updateFuturesOrder(futuresOrder.getWallet().getId(), futuresOrder);
    }



}
