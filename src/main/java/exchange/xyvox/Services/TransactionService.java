package exchange.xyvox.Services;

import exchange.xyvox.Models.Coin;
import exchange.xyvox.Models.Enums.TransactionStatusEnum;
import exchange.xyvox.Models.Enums.TransactionTypeEnum;
import exchange.xyvox.Models.Transaction;
import exchange.xyvox.Models.Wallet;
import exchange.xyvox.Repositories.CoinRepository;
import exchange.xyvox.Repositories.TransactionRepository;
import exchange.xyvox.Repositories.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CoinRepository coinRepository;
    private final WalletRepository walletRepository;

    public TransactionService(TransactionRepository transactionRepository, CoinRepository coinRepository, WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.coinRepository = coinRepository;
        this.walletRepository = walletRepository;
    }

    public Transaction createTransaction(
            Integer walletId,
            Integer coinId,
            TransactionTypeEnum type,
            Double amount,
            Double value
    ) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));
        Coin coin = coinRepository.findById(coinId)
                .orElseThrow(() -> new EntityNotFoundException("Coin not found"));

        Timestamp now = new Timestamp(System.currentTimeMillis());

        Transaction tx = new Transaction(type, amount, value, now,
                TransactionStatusEnum.PENDING, coin, wallet);

        wallet.getTransactions().add(tx);

        return transactionRepository.save(tx);
    }

    public void updateTransactionStatusById(Integer transactionId, TransactionStatusEnum status) {
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if(transaction == null) {
            throw new EntityNotFoundException("Transaction not found");
        }
        transaction.setStatus(status);
        transactionRepository.save(transaction);
    }


}
