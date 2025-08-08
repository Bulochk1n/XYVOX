package exchange.xyvox.Repositories;

import exchange.xyvox.Models.FuturesPosition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuturesPositionRepository extends CrudRepository<FuturesPosition, Integer> {
}
