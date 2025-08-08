package exchange.xyvox.Repositories;

import exchange.xyvox.Models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {
    Optional<Wallet> findWalletById(Integer id);

    @Query("SELECT w " +
            "FROM Wallet w JOIN w.addresses addr " +
            "WHERE KEY(addr) = :network AND addr = :address")
    Optional<Wallet> findByNetworkAndAddress(
            @Param("network") String network,
            @Param("address") String address
    );}
