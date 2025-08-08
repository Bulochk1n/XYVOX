package exchange.xyvox.Services;

import exchange.xyvox.Models.Coin;
import exchange.xyvox.Repositories.CoinRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CoinService {

    private final CoinRepository coinRepository;


    public CoinService(CoinRepository coinRepository) {
        this.coinRepository = coinRepository;
    }

    public List<String> getAllCoinNames() {
        List<String> coinNames = new ArrayList<>();
        coinRepository.findAll().forEach(coin -> coinNames.add(coin.getName()));
        return coinNames;
    };

    public List<String> getAllCoinSymbols() {
        List<String> coinSymbols = new ArrayList<>();
        coinRepository.findAll().forEach(coin -> coinSymbols.add(coin.getSymbol()));
        return coinSymbols;
    }

    public Coin getCoinByName(String name) {

        return coinRepository.findCoinByName(name).orElse(null);
    }

    public Coin getCoinBySymbol(String symbol) {
        return coinRepository.findCoinBySymbol(symbol).orElse(null);
    }

    public Map<String, Double> fetchCurrentPricesForSymbols(List<String> symbols) {
        Map<String, Double> currentPrices = new HashMap<>();
        for (String symbol : symbols) {
            Coin coin = getCoinBySymbol(symbol);
            if (coin != null) {
                currentPrices.put(symbol, coin.getPrice());
            }
        }
        return currentPrices;
    }
}
