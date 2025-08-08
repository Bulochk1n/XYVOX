package exchange.xyvox.Authentication;


import exchange.xyvox.Models.AppUser;
import exchange.xyvox.Repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Autowired
    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        //Instead of username for login email is used

        AppUser appUser = appUserRepository.findByEmail(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + login));
        return new CustomUserDetails(appUser);
    }


}
