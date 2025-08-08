package exchange.xyvox.Repositories;

import exchange.xyvox.Models.Enums.OrderStatusEnum;
import exchange.xyvox.Models.SpotOrder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpotOrderRepository extends CrudRepository<SpotOrder, Integer> {
    List<SpotOrder> findByStatus(OrderStatusEnum status);
}
