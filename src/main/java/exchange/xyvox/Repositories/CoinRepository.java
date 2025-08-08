package exchange.xyvox.Repositories;

import exchange.xyvox.Models.Coin;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoinRepository extends CrudRepository<Coin, Integer> {
    Optional<Coin> findCoinById(Integer id);
    Optional<Coin> findCoinByName(String name);
    Optional<Coin> findCoinBySymbol(String symbol);
}
