package exchange.xyvox.Configs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PasswordConfig {
    @Bean
    public PasswordEncoder passwordEncoder(@Qualifier("customEncoders") Map<String, PasswordEncoder> customEncoders) {
        String defaultId = "bcrypt";
        return new DelegatingPasswordEncoder(defaultId, customEncoders);
    }

    @Bean("customEncoders")
    public Map<String, PasswordEncoder> encodersMap() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();

        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("pbkdf2", new Pbkdf2PasswordEncoder("", 16, 18500, 256));
        encoders.put("scrypt", new SCryptPasswordEncoder(16384, 8,1, 32,64));
        encoders.put("argon2", new Argon2PasswordEncoder(16,32,1,1 << 12, 3));


        return encoders;
    }

}
