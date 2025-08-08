package exchange.xyvox.Services;


import exchange.xyvox.Models.AppUser;
import exchange.xyvox.Models.Role;
import exchange.xyvox.Repositories.AppUserRepository;
import exchange.xyvox.Repositories.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder delegatingPasswordEncoder;
    private final Map<String, PasswordEncoder> passwordEncodersMap;
    private final Random rand = new SecureRandom();
    private final List<String> encoders = new ArrayList<>();


    public AppUserService(AppUserRepository appUserRepository, RoleRepository roleRepository, PasswordEncoder delegatingPasswordEncoder, Map<String, PasswordEncoder> passwordEncodersMap) {

        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.delegatingPasswordEncoder = delegatingPasswordEncoder;
        this.passwordEncodersMap = passwordEncodersMap;

        this.encoders.addAll(passwordEncodersMap.keySet());
    }

    public AppUser getUserByUsername(String username) {
        return appUserRepository.findByUsername(username).orElse(null);
    }

    public AppUser getUserByEmail(String email) {
        return appUserRepository.findByEmail(email).orElse(null);
    }

    public void addNewUser(AppUser user) {
        appUserRepository.save(user);
    }

    @Transactional
    public void updateProfile(
            String currentEmail,
            String newUsername,
            String newEmail,
            String newPassword
    ) {
        AppUser user = getUserByEmail(currentEmail);

        user.setUsername(newUsername);
        user.setEmail(newEmail);

        if (newPassword != null && !newPassword.isBlank()) {
            int idx = rand.nextInt(encoders.size());
            String chosenId = encoders.get(idx);

            PasswordEncoder specificEncoder = passwordEncodersMap.get(chosenId);
            if (specificEncoder == null) {
                throw new IllegalStateException("Encoder not found for id: " + chosenId);
            }
            String hashed = specificEncoder.encode(newPassword);
            String passwordWithPrefix = "{" + chosenId + "}" + hashed;
            user.setPassword(passwordWithPrefix);
        }

        appUserRepository.save(user);
    }



    public List<AppUser> getAllUsers(){
        return (List<AppUser>) appUserRepository.findAll();
    }

    public void deleteUserByName(String username) {
        AppUser user = appUserRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found: " + username)
        );
        appUserRepository.delete(user);
    }

    public void assignRoleToUser(String username, String rolename) {
        AppUser user = appUserRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found: " + username)
        );
        Role role = roleRepository.findByRoleName(rolename).orElseThrow(
                () -> new EntityNotFoundException("Role not found: " + rolename)
        );
        if(user.getRole().equals(role)) {
            return;
        }
        user.setRole(role);
        appUserRepository.save(user);

    }

    public boolean hasRole(String username, String rolename) {
        AppUser user = appUserRepository.findByUsername(username).orElse(null);
        if(user == null){
            return false;
        }
        if(user.getRole().getRoleName().equals(rolename)) {
            return true;
        }
        return false;
    }

}
