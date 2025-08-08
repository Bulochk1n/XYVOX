package exchange.xyvox.Repositories;

import exchange.xyvox.Models.AppUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends CrudRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String email);
    Optional<AppUser> findByEmail(String email);
}
