package exchange.xyvox.Controllers;

import exchange.xyvox.Models.*;
import exchange.xyvox.Models.Enums.TransactionTypeEnum;
import exchange.xyvox.Services.AppUserService;
import exchange.xyvox.Services.CoinService;
import exchange.xyvox.Services.TransactionService;
import exchange.xyvox.Services.WalletService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/xyvox/api/v1/wallet")
public class WalletController {

    private final CoinService coinService;
    private final WalletService walletService;

    private final List<String> supportedNetworks = Arrays.asList(
            "Ethereum", "Bitcoin", "Tron", "Binance Smart Chain"
    );
    private final AppUserService appUserService;
    private final TransactionService transactionService;

    public WalletController(CoinService coinService, WalletService walletService, AppUserService appUserService, TransactionService transactionService) {
        this.coinService = coinService;
        this.walletService = walletService;
        this.appUserService = appUserService;
        this.transactionService = transactionService;
    }

    @GetMapping("/deposit")
    public String depositForm(
            Authentication authentication,
            @RequestParam(value = "coinName", required = false) String coinName,
            @RequestParam(value = "network", required = false) String network,
            Model model
    ) {
        List<String> coinNames = coinService.getAllCoinNames();
        model.addAttribute("coinNames", coinNames);

        model.addAttribute("networks", supportedNetworks);

        if (coinName != null && network != null && !network.isBlank()) {

            String userEmail = authentication.getName();
            AppUser user = appUserService.getUserByEmail(userEmail);
            Wallet wallet = user.getWallet();

            String depositAddress = walletService.getAddress(wallet.getId(), network);
            model.addAttribute("depositAddress", depositAddress);

            model.addAttribute("selectedCoinName", coinName);
            model.addAttribute("selectedNetwork", network);
        }
        return "deposit-form";
    }

    @PostMapping("/deposit")
    public String depositSubmit(
            Authentication authentication,
            @RequestParam("coinName") @NotNull String coinName,
            @RequestParam("network") @NotNull String network
    ) {

        return "redirect:/xyvox/api/v1/wallet/deposit?coinName=" + coinName + "&network=" + network;
    }

    @GetMapping("/withdraw")
    public String withdrawForm(
            Authentication authentication,
            @RequestParam(value = "coinName", required = false) String coinName,
            @RequestParam(value = "network", required = false) String network,
            @RequestParam(value = "balance", required = false) Double balance,
            Model model,
            @ModelAttribute("message") String flashMessage) {

        AppUser user = appUserService.getUserByEmail(authentication.getName());
        Wallet wallet = user.getWallet();
        Set<WalletCoin> walletCoins = wallet.getWalletCoins();

        model.addAttribute("coins", walletCoins);
        model.addAttribute("networks", supportedNetworks);

        if (coinName != null && network != null && balance != null) {
            model.addAttribute("selectedCoin", coinName);
            model.addAttribute("selectedNetwork", network);
            model.addAttribute("balance", balance);
        }

        if (flashMessage != null && !flashMessage.isEmpty()) {
            model.addAttribute("message", flashMessage);
        }

        return "withdraw-form";
    }

    @PostMapping("/withdraw/balance")
    public String withdrawBalance(
            Authentication authentication,
            @RequestParam("coinName") @NotNull String coinName,
            @RequestParam("network") @NotNull String network,
            RedirectAttributes redirectAttributes) {

        AppUser user = appUserService.getUserByEmail(authentication.getName());
        Wallet wallet = user.getWallet();
        Set<WalletCoin> walletCoins = wallet.getWalletCoins();
        Coin coin = coinService.getCoinByName(coinName);


        WalletCoin found = null;
        for (WalletCoin wc : walletCoins) {
            if (wc.getCoin().equals(coin)) {
                found = wc;
                break;
            }
        }

        if (found != null) {
            Double balance = found.getAmount();

            return "redirect:/xyvox/api/v1/wallet/withdraw"
                    + "?coinName=" + coinName
                    + "&network=" + network
                    + "&balance=" + balance;
        }

        redirectAttributes.addFlashAttribute("notEnoughBalance", "You don't have enough balance to withdraw");
        return "redirect:/xyvox/api/v1/wallet/withdraw";
    }

    @PostMapping("/withdraw")
    public String withdrawSubmit(
            Authentication authentication,
            @RequestParam("coinName") @NotNull String coinName,
            @RequestParam("network") @NotNull String network,
            @RequestParam("amount") @NotNull Double amount,
            @RequestParam("recipientAddress")
            @NotNull @Size(min = 42, message = "Address size is 42 characters long") String recipientAddress,
            RedirectAttributes redirectAttributes) {

        AppUser user = appUserService.getUserByEmail(authentication.getName());
        Wallet wallet = user.getWallet();
        Set<WalletCoin> walletCoins = wallet.getWalletCoins();
        Coin coin = coinService.getCoinByName(coinName);


        WalletCoin found = null;
        for (WalletCoin wc : walletCoins) {
            if (wc.getCoin().equals(coin)) {
                found = wc;
                break;
            }
        }

        if (found == null) {
            redirectAttributes.addFlashAttribute("walletNotExist", "Provided wallet does not exist");
            return "redirect:/xyvox/api/v1/wallet/withdraw";
        }

        Double balance = found.getAmount();
        if (amount > balance) {
            redirectAttributes.addFlashAttribute("message", "You don't have enough funds.");
            return "redirect:/xyvox/api/v1/wallet/withdraw"
                    + "?coinName=" + coinName
                    + "&network=" + network
                    + "&balance=" + balance;
        }


        Wallet recipientWallet = walletService.getWalletByNetworkAndAddress(network, recipientAddress);
        Integer recipientWalletId = recipientWallet != null ? recipientWallet.getId() : -1;

        if (recipientWalletId == -1){
            redirectAttributes.addFlashAttribute("walletNotExist", "Wallet does not exist.");
            return "redirect:/xyvox/api/v1/wallet/withdraw";
        }

        Transaction transaction_recipient = transactionService.createTransaction(recipientWalletId, coin.getId(), TransactionTypeEnum.DEPOSIT,
                amount, coin.getPrice()*amount);
        walletService.sendCoins(coin, amount, recipientAddress, network, transaction_recipient.getId());
        Transaction transaction_sender = transactionService.createTransaction(wallet.getId(), coin.getId(), TransactionTypeEnum.WITHDRAWAL,
                amount, coin.getPrice()*amount);
        walletService.updateCoinAmount(wallet.getId(), coin, amount, "sell", transaction_sender.getId());

        return "redirect:/xyvox/api/v1/users/my-profile";
    }

    @GetMapping("transactions")
    public String transactions(Authentication authentication, Model model) {
        AppUser user = appUserService.getUserByEmail(authentication.getName());
        Wallet wallet = user.getWallet();
        Set<Transaction> transactions = wallet.getTransactions();
        model.addAttribute("transactions", transactions);
        return "transactions";
    }






}
