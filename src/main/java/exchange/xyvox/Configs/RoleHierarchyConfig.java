package exchange.xyvox.Configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@Configuration
public class RoleHierarchyConfig {
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl h = new RoleHierarchyImpl();
        String hierarchy =
                "ROLE_ADMIN > ROLE_MODERATOR\n" +
                        "ROLE_MODERATOR > ROLE_USER";
        h.setHierarchy(hierarchy);
        return h;
    }
}
