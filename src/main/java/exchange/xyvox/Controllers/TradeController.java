package exchange.xyvox.Controllers;

import exchange.xyvox.Models.*;
import exchange.xyvox.Models.Enums.FuturesPositionStatusEnum;
import exchange.xyvox.Models.Enums.OrderStatusEnum;
import exchange.xyvox.Models.Enums.TransactionStatusEnum;
import exchange.xyvox.Models.Enums.TransactionTypeEnum;
import exchange.xyvox.Services.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Controller
@RequestMapping("/xyvox/api/v1/trade")
public class TradeController {

    private final CoinService coinService;
    private final WalletService walletService;
    private final AppUserService appUserService;
    private final TransactionService transactionService;
    private final OrderService orderService;
    private final FuturesPositionService futuresPositionService;
    private final PriceUpdateService priceUpdateService;

    private Double perpsPercentage = 0.8;


    public TradeController(CoinService coinService, WalletService walletService, AppUserService appUserService, TransactionService transactionService, OrderService orderService, FuturesPositionService futuresPositionService, PriceUpdateService priceUpdateService) {
        this.coinService = coinService;
        this.walletService = walletService;
        this.appUserService = appUserService;
        this.transactionService = transactionService;
        this.orderService = orderService;
        this.futuresPositionService = futuresPositionService;
        this.priceUpdateService = priceUpdateService;
    }



    @GetMapping()
    public String trade(@RequestParam(name = "symbolLast", required = false) String symbolLast, Model model, Authentication authentication, Map map) {

        Map<String, String> symbolNames = new HashMap<>();
        List<String> allowedSymbols = new java.util.ArrayList<>(coinService.getAllCoinSymbols().stream().map(
                symbol -> "BINANCE:" + symbol + "USDT"
        ).toList());
        allowedSymbols.remove("BINANCE:USDTUSDT");

        List<String> allSymbols = new java.util.ArrayList<>(coinService.getAllCoinSymbols().stream().map(symbol -> symbol + "/USDT").toList());
        allSymbols.remove("USDT/USDT");



        for (int i = 0; i < allSymbols.size(); i++) {
            symbolNames.put(allowedSymbols.get(i), allSymbols.get(i));
        }


        int tokenId = 0;
        if(symbolLast != null && allowedSymbols.contains(symbolLast)) {
            tokenId = allowedSymbols.indexOf(symbolLast);
        }

        if (authentication != null) {
            AppUser user = appUserService.getUserByEmail(authentication.getName());
            Wallet wallet = user.getWallet();
            Double balanceUSDT = walletService.getWalletCoinAmount(wallet.getId(), "USDT");
            model.addAttribute("balanceUSDT", balanceUSDT);

            List<WalletCoin> walletCoins = new ArrayList<>(wallet.getWalletCoins());
            walletCoins.sort(Comparator.comparing(WalletCoin::getCoinvalue).reversed());
            model.addAttribute("walletCoins", walletCoins);

            List<SpotOrder> spotOrders = new ArrayList<>(wallet.getSpotOrders());
            List<FuturesOrder> futuresOrders = new ArrayList<>(wallet.getFuturesOrders());

            if(!spotOrders.isEmpty() || !futuresOrders.isEmpty()) {
                List<SpotOrder> spotOrdersOpen = new ArrayList<>(spotOrders.stream().filter(order -> order.getStatus().equals(OrderStatusEnum.OPEN)).toList());
                List<SpotOrder> spotOrderHistory = new ArrayList<>(spotOrders.stream().filter(order -> !order.getStatus().equals(OrderStatusEnum.OPEN)).toList());

                List<FuturesOrder> futuresOrdersOpen = new ArrayList<>(futuresOrders.stream().filter(order -> order.getStatus().equals(OrderStatusEnum.OPEN)).toList());
                List<FuturesOrder> futuresOrderHistory = new ArrayList<>(futuresOrders.stream().filter(order -> !order.getStatus().equals(OrderStatusEnum.OPEN)).toList());



                spotOrdersOpen.sort(Comparator.comparing(SpotOrder::getCreatedAt).reversed());

                futuresOrdersOpen.sort(Comparator.comparing(FuturesOrder::getCreatedAt).reversed());

                List<Order> allOrdersHistory = new ArrayList<>();
                allOrdersHistory.addAll(futuresOrderHistory);
                allOrdersHistory.addAll(spotOrderHistory);

                allOrdersHistory.sort(Comparator.comparing(Order::getCreatedAt).reversed());


                model.addAttribute("spotOrdersOpen", spotOrdersOpen);
                model.addAttribute("futuresOrdersOpen", futuresOrdersOpen);
                model.addAttribute("allOrdersHistory", allOrdersHistory);
            }

            List<FuturesPosition> futuresPositions = new ArrayList<>(wallet.getFuturesPositions());
            if(!futuresPositions.isEmpty()) {
                List<FuturesPosition> futuresPositionsOpen = new ArrayList<>(futuresPositions.stream().filter(position -> position.getStatus().equals(FuturesPositionStatusEnum.OPEN) ).toList());
                List<FuturesPosition> futuresPositionsHistory = new ArrayList<>(futuresPositions.stream().filter(position -> !position.getStatus().equals(FuturesPositionStatusEnum.OPEN) ).toList());

                futuresPositionsOpen.sort(Comparator.comparing(FuturesPosition::getOpenedAt).reversed());
                futuresPositionsHistory.sort(Comparator.comparing(FuturesPosition::getOpenedAt).reversed());

                model.addAttribute("futuresPositionsOpen", futuresPositionsOpen);
                model.addAttribute("futuresPositionsHistory", futuresPositionsHistory);
            }


        }


        model.addAttribute("symbolNames", symbolNames);
        model.addAttribute("allowedSymbols", allowedSymbols);
        model.addAttribute("tokenId", tokenId);
        return "tradingview";
    }

    @PostMapping("/perps/market")
    public String perpsOpenMarketOrder(
            Authentication authentication,
            @RequestParam(name = "symbol", required = false) String symbol,
            @RequestParam(name = "leverage", required = false) String leverage_str,
            @RequestParam(name = "size", required = false) String size_str,
            @RequestParam(name = "stopLoss", required = false) String stopLoss_str,
            @RequestParam(name = "takeProfit", required = false) String takeProfit_str,
            @RequestParam(name = "side", required = false) String side,
            RedirectAttributes redirectAttributes
    ) {

        if (symbol == null || symbol.isEmpty()
                || leverage_str == null || leverage_str.isEmpty()
                || size_str == null || size_str.isEmpty()
                || side == null || side.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Missing required parameters.");
            return "redirect:/xyvox/api/v1/trade";
        }

        side = side.toLowerCase();
        if (!"long".equals(side) && !"short".equals(side)) {
            redirectAttributes.addFlashAttribute("error", "Side must be 'long' or 'short'.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }

        if (!leverage_str.endsWith("x")) {
            redirectAttributes.addFlashAttribute("error", "Leverage format is invalid (пример: \"10x\").");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }
        String levNum = leverage_str.substring(0, leverage_str.length() - 1);
        if (!levNum.matches("\\d+")) {
            redirectAttributes.addFlashAttribute("error", "Leverage must be a whole number followed by 'x'.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }
        int leverage = Integer.parseInt(levNum);
        int maxLeverage = 100;
        if (leverage < 1 || leverage > maxLeverage) {
            redirectAttributes.addFlashAttribute("error", "Leverage must be between 1x and " + maxLeverage + "x.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }

        double size;
        try {
            size = Double.parseDouble(size_str);
            if (size <= 0) {
                redirectAttributes.addFlashAttribute("error", "Size must be greater than zero.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        } catch (NumberFormatException ex) {
            redirectAttributes.addFlashAttribute("error", "Size must be a valid number.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }

        double stopLoss = -1, takeProfit = -1;
        if (stopLoss_str != null && !stopLoss_str.isEmpty()) {
            try {
                stopLoss = Double.parseDouble(stopLoss_str);
                if (stopLoss <= 0) {
                    redirectAttributes.addFlashAttribute("error", "StopLoss must be greater than zero.");
                    return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
                }
            } catch (NumberFormatException ex) {
                redirectAttributes.addFlashAttribute("error", "StopLoss must be a valid number.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        }
        if (takeProfit_str != null && !takeProfit_str.isEmpty()) {
            try {
                takeProfit = Double.parseDouble(takeProfit_str);
                if (takeProfit <= 0) {
                    redirectAttributes.addFlashAttribute("error", "TakeProfit must be greater than zero.");
                    return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
                }
            } catch (NumberFormatException ex) {
                redirectAttributes.addFlashAttribute("error", "TakeProfit must be a valid number.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        }

        String clearSymbol = extractSymbol(symbol);
        if (clearSymbol == null || clearSymbol.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Symbol format is invalid.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }

        AppUser user = appUserService.getUserByEmail(authentication.getName());
        Wallet wallet = user.getWallet();

        Coin coin = coinService.getCoinBySymbol(clearSymbol);
        if (coin == null) {
            redirectAttributes.addFlashAttribute("error", "Unknown symbol.");
            return "redirect:/xyvox/api/v1/trade";
        }
        double price = coin.getPrice();
        if (price <= 0) {
            redirectAttributes.addFlashAttribute("error", "Current price for symbol is invalid.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }

        if ((side.equals("long") && price < coin.getPrice() * perpsPercentage) ||
                (side.equals("short") && price > coin.getPrice() * (2 - perpsPercentage)) ||
                (side.equals("long") && price > coin.getPrice() * (2 - perpsPercentage)) ||
                (side.equals("short") && price < coin.getPrice() * perpsPercentage)) {

            redirectAttributes.addFlashAttribute("invalidExecPrice", "Invalid execution price.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        double minDistance = price * 0.001;
        if (stopLoss > 0) {
            if (side.equals("long") && stopLoss >= price) {
                redirectAttributes.addFlashAttribute("error", "StopLoss for long must be below current price.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
            if (side.equals("short") && stopLoss <= price) {
                redirectAttributes.addFlashAttribute("error", "StopLoss for short must be above current price.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
            if (Math.abs(stopLoss - price) < minDistance) {
                redirectAttributes.addFlashAttribute("error", "StopLoss is too close to the current price.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        }
        if (takeProfit > 0) {
            if (side.equals("long") && takeProfit <= price) {
                redirectAttributes.addFlashAttribute("error", "TakeProfit for long must be above current price.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
            if (side.equals("short") && takeProfit >= price) {
                redirectAttributes.addFlashAttribute("error", "TakeProfit for short must be below current price.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
            if (Math.abs(takeProfit - price) < minDistance) {
                redirectAttributes.addFlashAttribute("error", "TakeProfit is too close to the current price.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        }
        if (stopLoss > 0 && takeProfit > 0 && stopLoss == takeProfit) {
            redirectAttributes.addFlashAttribute("error", "StopLoss and TakeProfit cannot be equal.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        double amount = size * leverage / price;
        TransactionTypeEnum type = side.equals("long") ? TransactionTypeEnum.LONG : TransactionTypeEnum.SHORT;
        Transaction transaction = transactionService.createTransaction(
                wallet.getId(), coin.getId(), type, amount, size
        );
        FuturesOrder futuresOrder = orderService.createFuturesOrder(
                clearSymbol, side, price, amount, size, leverage, stopLoss, takeProfit, wallet.getId(), "market", transaction
        );
        priceUpdateService.executeFuturesOrder(futuresOrder, coin.getPrice());

        String redirectUrl = UriComponentsBuilder.fromPath("/xyvox/api/v1/trade")
                .queryParam("symbolLast", symbol)
                .build()
                .toUriString();
        return "redirect:" + redirectUrl;
    }


    @PostMapping("/perps/limit")
    public String perpsOpenLimitOrder(
            Authentication authentication,
            @RequestParam(name = "symbol", required = false) String symbol,
            @RequestParam(name = "price", required = false) String price_str,
            @RequestParam(name = "leverage", required = false) String leverage_str,
            @RequestParam(name = "size", required = false) String size_str,
            @RequestParam(name = "stopLoss", required = false) String stopLoss_str,
            @RequestParam(name = "takeProfit", required = false) String takeProfit_str,
            @RequestParam(name = "side", required = false) String side,
            RedirectAttributes redirectAttributes
    ) {

        if (symbol == null || symbol.isEmpty()
                || price_str == null || price_str.isEmpty()
                || leverage_str == null || leverage_str.isEmpty()
                || size_str == null || size_str.isEmpty()
                || side == null || side.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Missing required parameters.");
            return "redirect:/xyvox/api/v1/trade";
        }


        side = side.toLowerCase();
        if (!"long".equals(side) && !"short".equals(side)) {
            redirectAttributes.addFlashAttribute("error", "Side must be 'long' or 'short'.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        if (!leverage_str.endsWith("x")) {
            redirectAttributes.addFlashAttribute("error", "Leverage format is invalid (пример: \"10x\").");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }
        String levNum = leverage_str.substring(0, leverage_str.length() - 1);
        if (!levNum.matches("\\d+")) {
            redirectAttributes.addFlashAttribute("error", "Leverage must be a whole number followed by 'x'.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }
        int leverage = Integer.parseInt(levNum);
        int maxLeverage = 100;
        if (leverage < 1 || leverage > maxLeverage) {
            redirectAttributes.addFlashAttribute("error", "Leverage must be between 1x and " + maxLeverage + "x.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        double price;
        try {
            price = Double.parseDouble(price_str);
            if (price <= 0) {
                redirectAttributes.addFlashAttribute("error", "Price must be greater than zero.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        } catch (NumberFormatException ex) {
            redirectAttributes.addFlashAttribute("error", "Price must be a valid number.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        double size;
        try {
            size = Double.parseDouble(size_str);
            if (size <= 0) {
                redirectAttributes.addFlashAttribute("error", "Size must be greater than zero.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        } catch (NumberFormatException ex) {
            redirectAttributes.addFlashAttribute("error", "Size must be a valid number.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        double stopLoss = -1, takeProfit = -1;
        if (stopLoss_str != null && !stopLoss_str.isEmpty()) {
            try {
                stopLoss = Double.parseDouble(stopLoss_str);
                if (stopLoss <= 0) {
                    redirectAttributes.addFlashAttribute("error", "StopLoss must be greater than zero.");
                    return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
                }
            } catch (NumberFormatException ex) {
                redirectAttributes.addFlashAttribute("error", "StopLoss must be a valid number.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        }
        if (takeProfit_str != null && !takeProfit_str.isEmpty()) {
            try {
                takeProfit = Double.parseDouble(takeProfit_str);
                if (takeProfit <= 0) {
                    redirectAttributes.addFlashAttribute("error", "TakeProfit must be greater than zero.");
                    return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
                }
            } catch (NumberFormatException ex) {
                redirectAttributes.addFlashAttribute("error", "TakeProfit must be a valid number.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        }


        String clearSymbol = extractSymbol(symbol);
        if (clearSymbol == null || clearSymbol.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Symbol format is invalid.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }

        AppUser user = appUserService.getUserByEmail(authentication.getName());
        Wallet wallet = user.getWallet();

        Coin coin = coinService.getCoinBySymbol(clearSymbol);
        if (coin == null) {
            redirectAttributes.addFlashAttribute("error", "Unknown symbol.");
            return "redirect:/xyvox/api/v1/trade";
        }
        double marketPrice = coin.getPrice();
        if (marketPrice <= 0) {
            redirectAttributes.addFlashAttribute("error", "Current price for symbol is invalid.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }

        if ((side.equals("long") && price < marketPrice * perpsPercentage) ||
                (side.equals("short") && price > marketPrice * (2 - perpsPercentage)) ||
                (side.equals("long") && price > marketPrice * (2 - perpsPercentage)) ||
                (side.equals("short") && price < marketPrice * perpsPercentage)) {

            redirectAttributes.addFlashAttribute("invalidExecPrice", "Invalid execution price.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        double minDistance = price * 0.001;
        if (stopLoss > 0) {
            if (side.equals("long") && stopLoss >= price) {
                redirectAttributes.addFlashAttribute("error", "StopLoss for long must be below order price.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
            if (side.equals("short") && stopLoss <= price) {
                redirectAttributes.addFlashAttribute("error", "StopLoss for short must be above order price.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
            if (Math.abs(stopLoss - price) < minDistance) {
                redirectAttributes.addFlashAttribute("error", "StopLoss is too close to the order price.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        }
        if (takeProfit > 0) {
            if (side.equals("long") && takeProfit <= price) {
                redirectAttributes.addFlashAttribute("error", "TakeProfit for long must be above order price.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
            if (side.equals("short") && takeProfit >= price) {
                redirectAttributes.addFlashAttribute("error", "TakeProfit for short must be below order price.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
            if (Math.abs(takeProfit - price) < minDistance) {
                redirectAttributes.addFlashAttribute("error", "TakeProfit is too close to the order price.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        }
        if (stopLoss > 0 && takeProfit > 0 && stopLoss == takeProfit) {
            redirectAttributes.addFlashAttribute("error", "StopLoss and TakeProfit cannot be equal.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        double requiredMargin = size / leverage;
        double availableUsdt = walletService.getWalletCoinAmount(wallet.getId(), "USDT");
        if (requiredMargin > availableUsdt) {
            redirectAttributes.addFlashAttribute("notEnoughBalance", "Not enough margin (USDT) to open position.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        double amount = size * leverage / price;
        TransactionTypeEnum type = side.equals("long") ? TransactionTypeEnum.LONG : TransactionTypeEnum.SHORT;
        Transaction transaction = transactionService.createTransaction(
                wallet.getId(), coin.getId(), type, amount, size
        );
        FuturesOrder futuresOrder = orderService.createFuturesOrder(
                clearSymbol, side, price, amount, size, leverage, stopLoss, takeProfit, wallet.getId(), "limit", transaction
        );
        walletService.addFuturesOrder(wallet.getId(), futuresOrder);

        String redirectUrl = UriComponentsBuilder.fromPath("/xyvox/api/v1/trade")
                .queryParam("symbolLast", symbol)
                .build()
                .toUriString();
        return "redirect:" + redirectUrl;
    }


    @PostMapping("/spot/market")
    public String spotOpenMarketOrder(
            Authentication authentication,
            @RequestParam(name = "symbol", required = false) String symbol,
            @RequestParam(name = "size", required = false) String size_str,
            @RequestParam(name = "side", required = false) String side,
            @RequestParam(name = "currentPrice", required = false) Double currentPrice,
            RedirectAttributes redirectAttributes
    ) {

        if (symbol == null || symbol.isEmpty()
                || size_str == null || size_str.isEmpty()
                || side == null || side.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Missing required parameters.");
            return "redirect:/xyvox/api/v1/trade";
        }


        side = side.toLowerCase();
        if (!"buy".equals(side) && !"sell".equals(side)) {
            redirectAttributes.addFlashAttribute("error", "Side must be 'buy' or 'sell'.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        double size;
        try {
            size = Double.parseDouble(size_str);
            if (size <= 0) {
                redirectAttributes.addFlashAttribute("error", "Size must be greater than zero.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        } catch (NumberFormatException ex) {
            redirectAttributes.addFlashAttribute("error", "Size must be a valid number.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        String clearSymbol = extractSymbol(symbol);
        if (clearSymbol == null || clearSymbol.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Symbol format is invalid.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }

        AppUser user = appUserService.getUserByEmail(authentication.getName());
        Wallet wallet = user.getWallet();

        Coin coin = coinService.getCoinBySymbol(clearSymbol);
        if (coin == null) {
            redirectAttributes.addFlashAttribute("error", "Unknown symbol.");
            return "redirect:/xyvox/api/v1/trade";
        }
        double marketPrice = coin.getPrice();
        if (marketPrice <= 0) {
            redirectAttributes.addFlashAttribute("error", "Current price for symbol is invalid.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }

        double availableUsdt = walletService.getWalletCoinAmount(wallet.getId(), "USDT");
        double availableCoinQty = walletService.getWalletCoinAmount(wallet.getId(), clearSymbol);

        if ("buy".equals(side)) {
            if (size > availableUsdt) {
                redirectAttributes.addFlashAttribute("notEnoughBalance", "You don't have enough USDT balance to buy.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        } else {
            double requiredCoinAmount = size / marketPrice;
            if (requiredCoinAmount > availableCoinQty) {
                redirectAttributes.addFlashAttribute("notEnoughBalance", "You don't have enough " + clearSymbol + " balance to sell.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        }


        double amount = size / marketPrice;
        TransactionTypeEnum type = side.equals("buy") ? TransactionTypeEnum.BUY : TransactionTypeEnum.SELL;

        Transaction transaction = transactionService.createTransaction(
                wallet.getId(),
                coin.getId(),
                type,
                amount,
                size
        );
        SpotOrder spotOrder = orderService.createSpotOrder(
                clearSymbol,
                side,
                marketPrice,
                amount,
                size,
                wallet.getId(),
                transaction,
                "market"
        );


        if ("buy".equals(side)) {
            walletService.updateCoinAmount(wallet.getId(), coinService.getCoinBySymbol("USDT"), size, "sell", transaction.getId());
            walletService.updateCoinAmount(wallet.getId(), coin, amount, "buy", transaction.getId());
        } else {
            walletService.updateCoinAmount(wallet.getId(), coin, amount, "sell", transaction.getId());
            walletService.updateCoinAmount(wallet.getId(), coinService.getCoinBySymbol("USDT"), size, "buy", transaction.getId());
        }
        walletService.addSpotOrder(wallet.getId(), spotOrder);


        String redirectUrl = UriComponentsBuilder.fromPath("/xyvox/api/v1/trade")
                .queryParam("symbolLast", symbol)
                .build()
                .toUriString();
        return "redirect:" + redirectUrl;
    }




    @PostMapping("/spot/limit")
    public String spotOpenLimitOrder(
            Authentication authentication,
            @RequestParam(name = "symbol", required = false) String symbol,
            @RequestParam(name = "price", required = false) String price_str,
            @RequestParam(name = "size", required = false) String size_str,
            @RequestParam(name = "side", required = false) String side,
            RedirectAttributes redirectAttributes
    ) {

        if (symbol == null || symbol.isEmpty()
                || price_str == null || price_str.isEmpty()
                || size_str == null || size_str.isEmpty()
                || side == null || side.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Missing required parameters.");
            return "redirect:/xyvox/api/v1/trade";
        }


        side = side.toLowerCase();
        if (!"buy".equals(side) && !"sell".equals(side)) {
            redirectAttributes.addFlashAttribute("error", "Side must be 'buy' or 'sell'.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }

        double price;
        try {
            price = Double.parseDouble(price_str);
            if (price <= 0) {
                redirectAttributes.addFlashAttribute("error", "Price must be greater than zero.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        } catch (NumberFormatException ex) {
            redirectAttributes.addFlashAttribute("error", "Price must be a valid number.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }

        double size;
        try {
            size = Double.parseDouble(size_str);
            if (size <= 0) {
                redirectAttributes.addFlashAttribute("error", "Size must be greater than zero.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        } catch (NumberFormatException ex) {
            redirectAttributes.addFlashAttribute("error", "Size must be a valid number.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        String clearSymbol = extractSymbol(symbol);
        if (clearSymbol == null || clearSymbol.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Symbol format is invalid.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }

        AppUser user = appUserService.getUserByEmail(authentication.getName());
        Wallet wallet = user.getWallet();

        Coin coin = coinService.getCoinBySymbol(clearSymbol);
        if (coin == null) {
            redirectAttributes.addFlashAttribute("error", "Unknown symbol.");
            return "redirect:/xyvox/api/v1/trade";
        }
        double marketPrice = coin.getPrice();
        if (marketPrice <= 0) {
            redirectAttributes.addFlashAttribute("error", "Current price for symbol is invalid.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        double availableUsdt = walletService.getWalletCoinAmount(wallet.getId(), "USDT");
        double availableCoinQty = walletService.getWalletCoinAmount(wallet.getId(), clearSymbol);
        if ("buy".equals(side)) {

            if (size > availableUsdt) {
                redirectAttributes.addFlashAttribute("notEnoughBalance", "Not enough USDT balance to buy.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        } else {

            double requiredCoinAmount = size / price;
            if (requiredCoinAmount > availableCoinQty) {
                redirectAttributes.addFlashAttribute("notEnoughBalance", "Not enough " + clearSymbol + " balance to sell.");
                return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
            }
        }


        if (("buy".equals(side) && price < marketPrice / 3)
                || ("buy".equals(side) && price >= marketPrice)
                || ("sell".equals(side) && price > marketPrice * 3)
                || ("sell".equals(side) && price <= marketPrice * 3)) {
            redirectAttributes.addFlashAttribute("invalidExecPrice", "Invalid execution price.");
            return "redirect:/xyvox/api/v1/trade?symbolLast=" + symbol;
        }


        double amount = size / price;
        TransactionTypeEnum type = "buy".equals(side)
                ? TransactionTypeEnum.BUY
                : TransactionTypeEnum.SELL;
        Transaction transaction = transactionService.createTransaction(
                wallet.getId(),
                coin.getId(),
                type,
                amount,
                size
        );
        SpotOrder spotOrder = orderService.createSpotOrder(
                clearSymbol,
                side,
                price,
                amount,
                size,
                wallet.getId(),
                transaction,
                "limit"
        );


        walletService.addSpotOrder(wallet.getId(), spotOrder);


        String redirectUrl = UriComponentsBuilder.fromPath("/xyvox/api/v1/trade")
                .queryParam("symbolLast", symbol)
                .build()
                .toUriString();
        return "redirect:" + redirectUrl;
    }


    public String extractSymbol(String symbolFull){
        System.out.println("extractSymbol " + symbolFull);
        String[] parts = symbolFull.split(":");
        return parts[1].substring(0, parts[1].length() - 4);
    }

    @PostMapping("/spot/limit/close")
    public String spotCloseLimitOrder(Authentication authentication,
                                      @RequestParam(name = "orderId", required = true) Integer orderId){

        SpotOrder spotOrder = orderService.findSpotOrderById(orderId);
        AppUser user = appUserService.getUserByEmail(authentication.getName());
        Wallet wallet = user.getWallet();
        Set<SpotOrder> spotOrders = wallet.getSpotOrders();
        for(SpotOrder order : spotOrders){
            if(order.equals(spotOrder)){
                orderService.updateSpotOrderStatus(order.getId(), OrderStatusEnum.CANCELLED);
                order.setStatus(OrderStatusEnum.CANCELLED);
                transactionService.updateTransactionStatusById(order.getTransaction().getId(), TransactionStatusEnum.CANCELLED);
                wallet.setSpotOrders(spotOrders);
                orderService.closeSpotOrder(order);
                return "redirect:/xyvox/api/v1/trade";
            }
        }

        return "redirect:/xyvox/api/v1/trade";
    }

    @PostMapping("/perps/limit/close")
    public String perpsCloseLimitOrder(Authentication authentication,
                                      @RequestParam(name = "orderId", required = true) Integer orderId){

        FuturesOrder futuresOrder = orderService.findFuturesOrderById(orderId);
        AppUser user = appUserService.getUserByEmail(authentication.getName());
        Wallet wallet = user.getWallet();
        Set<FuturesOrder> futuresOrders = wallet.getFuturesOrders();
        for(FuturesOrder order : futuresOrders){
            if(order.equals(futuresOrder)){
                orderService.updateFuturesOrderStatus(order.getId(), OrderStatusEnum.CANCELLED);
                order.setStatus(OrderStatusEnum.CANCELLED);
                transactionService.updateTransactionStatusById(order.getTransaction().getId(), TransactionStatusEnum.CANCELLED);
                wallet.setFuturesOrders(futuresOrders);
                orderService.closeFuturesOrder(order);
                return "redirect:/xyvox/api/v1/trade";
            }
        }

        return "redirect:/xyvox/api/v1/trade";
    }

    @PostMapping("/perps/position/close")
    public String perpsClosePosition(Authentication authentication,
                                     @RequestParam(name = "positionId", required = true) Integer positionId){
        System.out.println(" called close position ");
        System.out.println(positionId);
        FuturesPosition futuresPosition = futuresPositionService.findFuturesPositionById(positionId);
        AppUser user = appUserService.getUserByEmail(authentication.getName());
        Wallet wallet = user.getWallet();
        Set<FuturesPosition> futuresPositions = wallet.getFuturesPositions();
        for(FuturesPosition position : futuresPositions){
            if(position.equals(futuresPosition)){
                position = futuresPositionService.closeFuturesPosition(position.getId());
                transactionService.updateTransactionStatusById(position.getTransaction().getId(), TransactionStatusEnum.COMPLETED);
                wallet.setFuturesPositions(futuresPositions);

                return "redirect:/xyvox/api/v1/trade";
            }
        }
        return "redirect:/xyvox/api/v1/trade";
    }
}
