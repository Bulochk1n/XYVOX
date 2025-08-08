package exchange.xyvox.Services;


import exchange.xyvox.Models.Role;
import exchange.xyvox.Repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName).orElse(null);
    }

    public List<Role> getAllRoles() {
        return (List<Role>) roleRepository.findAll();
    }
}
